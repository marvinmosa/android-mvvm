package sw.es.viewmodel.weather;

import sw.es.viewmodel.abs.ViewModel;

/**
 * Created by albertopenasamor on 22/10/15.
 */
public interface AbsFavouriteWeathersViewModel extends ViewModel{
    void setup(FavouriteWeathersListener favouriteWeathersListener);
    void load();
    void pull(String localityName);
}
