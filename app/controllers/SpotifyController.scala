package controllers

import models.database.facade.service.{ServiceArtistTrait, SpotifyArtistFacade}
import models.service.api.{ApiFacade, SpotifyApiFacade}
import models.service.oauth.{OAuthRouting, SpotifyService}
import models.util.Constants

class SpotifyController extends StreamingServiceController with AnalysisController with AlbumInfoController {

  override val redirectionService: OAuthRouting = SpotifyService
  override val serviceName: String = Constants.serviceSpotify
  override val serviceArtistFacade: ServiceArtistTrait = SpotifyArtistFacade
  override val apiFacade: ApiFacade = SpotifyApiFacade
  override def serviceClass(identifier:Either[Int,String]) = SpotifyService(identifier)
}