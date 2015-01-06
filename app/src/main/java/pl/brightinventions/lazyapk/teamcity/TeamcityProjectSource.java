package pl.brightinventions.lazyapk.teamcity;

import android.util.Pair;

import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.OkHttpClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import pl.brightinventions.lazyapk.DownloadAbleApk;
import pl.brightinventions.lazyapk.ProjectApkSource;
import pl.brightinventions.lazyapk.ProjectApkSourceFetchResult;
import pl.brightinventions.lazyapk.ProjectOverview;
import pl.brightinventions.lazyapk.ProjectSource;
import pl.brightinventions.lazyapk.ProjectSourceFetchResult;
import pl.brightinventions.lazyapk.StringUtils;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;

public class TeamcityProjectSource implements ProjectSource {
    private static final Logger LOG = LoggerFactory.getLogger(TeamcityProjectSource.class.getSimpleName());
    private static CookieManager cookieManager;
    private final TeamcityCredentials credentials;
    private final Func0<TeamcityRemoteApi> apiFactory;
    private final String address;
    private TeamcityRemoteApi remoteApi;

    public TeamcityProjectSource(String address, TeamcityCredentials credentials) {
        this(address, credentials, makeTeamcityApiFactory(address, credentials));
    }

    private TeamcityProjectSource(String address, TeamcityCredentials credentials, Func0<TeamcityRemoteApi> apiFactory) {
        this.address = address;
        this.credentials = credentials;
        this.apiFactory = apiFactory;
    }

    private static Func0<TeamcityRemoteApi> makeTeamcityApiFactory(final String address, final TeamcityCredentials credentials) {
        return new Func0<TeamcityRemoteApi>() {
            @Override
            public TeamcityRemoteApi call() {
                OkHttpClient client = new OkHttpClient();
                cookieManager = new CookieManager();
                cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
                client.setCookieHandler(cookieManager);
                client.setAuthenticator(new Authenticator() {
                    @Override
                    public com.squareup.okhttp.Request authenticate(Proxy proxy, com.squareup.okhttp.Response response) throws IOException {
                        if (response.priorResponse() != null) {
                            LOG.warn("Failed to authenticate already {}", response);
                            return null;
                        }
                        com.squareup.okhttp.Request.Builder builder = response.request().newBuilder();
                        credentials.addCredentialsTo(builder);
                        return builder.build();
                    }

                    @Override
                    public com.squareup.okhttp.Request authenticateProxy(Proxy proxy, com.squareup.okhttp.Response response) throws IOException {
                        return authenticate(proxy, response);
                    }
                });
                Client retroClient = new OkClient(client);
                return new RestAdapter.Builder()
                        .setEndpoint(address).setClient(retroClient)
                        .setLogLevel(RestAdapter.LogLevel.BASIC)
                        .setConverter(new GsonConverter(new GsonBuilder().setDateFormat("yyyyMMdd'T'HHmmss").create()))
                        .setRequestInterceptor(new RequestInterceptor() {
                            @Override
                            public void intercept(RequestFacade request) {
                                request.addHeader("Accept", "application/json");
                            }
                        })
                        .build()
                        .create(TeamcityRemoteApi.class);
            }
        };
    }

    @Override
    public Observable<ProjectSourceFetchResult> fetchProjects() {
        return getRemoteApi().getProjects()
                .map(new Func1<TeamcityProjectsResponse, ProjectSourceFetchResult>() {
                    @Override
                    public ProjectSourceFetchResult call(TeamcityProjectsResponse teamcityProjectsResponse) {
                        ProjectSourceFetchResult fetchResult = new ProjectSourceFetchResult();
                        fetchResult.addResults(teamcityProjectsResponse.getValidProjects());
                        return fetchResult;
                    }
                });
    }

    public TeamcityRemoteApi getRemoteApi() {
        ensureRemoteApiClientCreated();
        return remoteApi;
    }

    private void ensureRemoteApiClientCreated() {
        if (remoteApi == null) {
            remoteApi = apiFactory.call();
        }
    }

    @Override
    public Observable<ProjectApkSourceFetchResult> fetchApkSources(ProjectOverview projectOverview) {
        TeamcityProjectResponse project = (TeamcityProjectResponse) projectOverview;
        return getRemoteApi().getProjectDetails(StringUtils.removeFirstCharIfSlash(project.href))
                .flatMapIterable(new Func1<TeamcityProjectResponse, Iterable<TeamcityProjectBuildType>>() {
                    @Override
                    public Iterable<TeamcityProjectBuildType> call(TeamcityProjectResponse teamcityProject) {
                        return teamcityProject.getBuildTypes();
                    }
                }).flatMap(new Func1<TeamcityProjectBuildType, Observable<TeamcityBuildTypeApkSourcesFetchResult>>() {
                    @Override
                    public Observable<TeamcityBuildTypeApkSourcesFetchResult> call(TeamcityProjectBuildType teamcityProjectBuildType) {
                        return fetchApkSourcesFromBuildType(teamcityProjectBuildType);
                    }
                })
                .reduce(new ProjectApkSourceFetchResult(), new Func2<ProjectApkSourceFetchResult, TeamcityBuildTypeApkSourcesFetchResult, ProjectApkSourceFetchResult>() {
                    @Override
                    public ProjectApkSourceFetchResult call(ProjectApkSourceFetchResult projectApkSourceFetchResult, TeamcityBuildTypeApkSourcesFetchResult teamcityBuildTypeApkSourcesFetchResult) {
                        projectApkSourceFetchResult.addResults(teamcityBuildTypeApkSourcesFetchResult.getResults());
                        projectApkSourceFetchResult.sortBy(new Func1<ProjectApkSource, Integer>() {
                            @Override
                            public Integer call(ProjectApkSource projectApkSource) {
                                return -Integer.valueOf(projectApkSource.getId());
                            }
                        });
                        return projectApkSourceFetchResult;
                    }
                });
    }

    @Override
    public Observable<ProjectApkSource> fetchApkSourceDetails(final ProjectApkSource apkSourceAt) {
        final TeamcityBuildResponse buildResponse = (TeamcityBuildResponse) apkSourceAt;
        LOG.trace("Fetch build details of {}", buildResponse);
        return getRemoteApi().getBuildDetails(StringUtils.removeFirstCharIfSlash(buildResponse.href))
                .flatMap(new Func1<TeamcityBuildResponse, Observable<TeamcityBuildResponse>>() {
                    @Override
                    public Observable<TeamcityBuildResponse> call(final TeamcityBuildResponse teamcityBuildResponse) {
                        if (teamcityBuildResponse.hasLastChange()) {
                            TeamcityBuildChange first = teamcityBuildResponse.lastChanges.first();
                            LOG.trace("Fetch build last change details {}", first);
                            return fetchBuildChangeDetails(first).map(new Func1<TeamcityBuildChange, TeamcityBuildResponse>() {
                                @Override
                                public TeamcityBuildResponse call(TeamcityBuildChange teamcityBuildChange) {
                                    teamcityBuildResponse.updateLastChangesDetails(teamcityBuildChange);
                                    return teamcityBuildResponse;
                                }
                            });
                        } else {
                            return Observable.just(teamcityBuildResponse);
                        }
                    }
                })
                .map(new Func1<TeamcityBuildResponse, ProjectApkSource>() {
                    @Override
                    public ProjectApkSource call(TeamcityBuildResponse teamcityBuildResponse) {
                        buildResponse.readDetailsFrom(teamcityBuildResponse);
                        return buildResponse;
                    }
                });
    }

    private Observable<TeamcityBuildChange> fetchBuildChangeDetails(final TeamcityBuildChange first) {
        String href = StringUtils.removeFirstCharIfSlash(first.href);
        return getRemoteApi().getBuildChangeDetails(href).map(new Func1<TeamcityBuildChange, TeamcityBuildChange>() {
            @Override
            public TeamcityBuildChange call(TeamcityBuildChange teamcityBuildChange) {
                first.readDetailsFrom(teamcityBuildChange);
                return first;
            }
        });
    }

    @Override
    public boolean hasProject(ProjectOverview projectOverview) {
        if (!(projectOverview instanceof TeamcityProjectResponse)) {
            return false;
        }
        TeamcityProjectResponse project = (TeamcityProjectResponse) projectOverview;
        return project.webUrl.startsWith(address);
    }

    private Observable<TeamcityBuildTypeApkSourcesFetchResult> fetchApkSourcesFromBuildType(final TeamcityProjectBuildType buildType) {
        return getRemoteApi().getBuildsForBuildType(StringUtils.removeFirstCharIfSlash(buildType.href)).map(new Func1<TeamcityBuildsResponse, TeamcityBuildTypeApkSourcesFetchResult>() {
            @Override
            public TeamcityBuildTypeApkSourcesFetchResult call(TeamcityBuildsResponse teamcityBuildsResponse) {
                TeamcityBuildTypeApkSourcesFetchResult result = new TeamcityBuildTypeApkSourcesFetchResult(buildType);
                result.addResults(teamcityBuildsResponse.build);
                return result;
            }
        });
    }

    public Observable<TeamcityBuildArtifactsFile> fetchArtifactsChildren(TeamcityBuildArtifactsFile directory) {
        return getRemoteApi().getArtifactFiles(StringUtils.removeFirstCharIfSlash(directory.children.href))
                .flatMapIterable(new Func1<TeamcityBuildArtifactsFiles, Iterable<TeamcityBuildArtifactsFile>>() {
                    @Override
                    public Iterable<TeamcityBuildArtifactsFile> call(TeamcityBuildArtifactsFiles teamcityBuildArtifactsFiles) {
                        return teamcityBuildArtifactsFiles.files;
                    }
                });
    }

    public DownloadAbleApk makeDownloadableApk(TeamcityBuildResponse buildResponse, TeamcityBuildArtifactsFile currentItem) {
        ensureRemoteApiClientCreated();
        String uniqueFileName = String.format("%s_%s_%s_%s", getHost(), buildResponse.getProjectOverView().getName(), buildResponse.getId(), currentItem.name);
        DownloadAbleApk apk = new DownloadAbleApk(StringUtils.appendSlashIfNotLast(address) + StringUtils.removeFirstCharIfSlash(currentItem.content.href), uniqueFileName);
        boolean addedCookie = false;
        try {
            List<HttpCookie> httpCookies = cookieManager.getCookieStore().get(new URI(currentItem.content.href));
            for (HttpCookie cookie : httpCookies) {
                apk.addRequestHeader("Cookie", cookie.toString());
                addedCookie = true;
            }
        } catch (URISyntaxException e) {
            LOG.warn("Failed to parse current item content href {}", currentItem.content.href, e);
        }
        if (!addedCookie) {
            for (Pair<String, String> header : credentials.getHeaders()) {
                apk.addRequestHeader(header.first, header.second);
            }
        }
        apk.setName(currentItem.name);
        return apk;
    }

    public String getHost() {
        try {
            URI uri = new URI(address);
            return uri.getHost();
        } catch (URISyntaxException e) {
            LOG.error("Failed to parse teamcity address {}", address, e);
            return address.replace("http://", "").replace("https://", "").replaceAll(":.*$", "").replaceAll("/.*$", "");
        }
    }

}
