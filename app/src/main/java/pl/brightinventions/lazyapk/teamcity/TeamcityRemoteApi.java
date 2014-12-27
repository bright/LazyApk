package pl.brightinventions.lazyapk.teamcity;

import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

public interface TeamcityRemoteApi {
    @GET("/httpAuth/app/rest/projects")
    Observable<TeamcityProjectsResponse> getProjects();

    @GET("/{href}")
    Observable<TeamcityProjectResponse> getProjectDetails(@Path(value = "href", encode = false) String href);

    @GET("/{href}/builds")
    Observable<TeamcityBuildsResponse> getBuildsForBuildType(@Path(value = "href", encode = false) String href);

    @GET("/{href}")
    Observable<TeamcityBuildResponse> getBuildDetails(@Path(value = "href", encode = false) String href);

    @GET("/{href}")
    Observable<TeamcityBuildChange> getBuildChangeDetails(@Path(value = "href", encode = false) String href);

    @GET("/{href}")
    Observable<TeamcityBuildArtifactsFiles> getArtifactFiles(@Path(value = "href", encode = false) String href);
}
