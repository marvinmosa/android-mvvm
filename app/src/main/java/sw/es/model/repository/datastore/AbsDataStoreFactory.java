package sw.es.model.repository.datastore;

import sw.es.model.repository.criteria.FetchCriteria;
import sw.es.model.repository.criteria.StoreCriteria;
import sw.es.model.repository.exceptions.NoMoreCriteriaException;
import sw.es.model.repository.outdate.Outdate;

/**
 * Created by alberto on 19/10/15.
 */
public abstract class AbsDataStoreFactory<Model> implements DataStoreFactory {


    private CloudDataStore<Model>cloudDataStore;
    private DBDataStore<Model>dbDataStore;
    private Outdate<Model> outdate;


    public AbsDataStoreFactory(CloudDataStore<Model> cloudDataStore, DBDataStore<Model> dbDataStore, Outdate<Model>outdate) {
        this.cloudDataStore = cloudDataStore;
        this.dbDataStore = dbDataStore;
        this.outdate = outdate;
    }

    @Override
    public DataStore get(FetchCriteria fetchCriteria) {
        if (fetchCriteria.getFetchCriteria().name().equals(FetchCriteria.FetchCriteriaEnum.GET.name())) {
            if (outdate.isExpired()) {
                try {
                    fetchCriteria.next();
                } catch (NoMoreCriteriaException e) {
                    e.printStackTrace();
                }
                return cloudDataStore;
            }
            return dbDataStore;
        } else if (fetchCriteria.getFetchCriteria().name().equals(FetchCriteria.FetchCriteriaEnum.REFRESH.name())) {
            return cloudDataStore;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public DataStore get(StoreCriteria storeCriteria) {
        if (storeCriteria.name().equals(StoreCriteria.SAVE.name())) {
            return cloudDataStore;
        } else {
            throw new IllegalArgumentException();
        }
    }
}
