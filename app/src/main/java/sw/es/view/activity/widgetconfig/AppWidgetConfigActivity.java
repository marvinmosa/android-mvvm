package sw.es.view.activity.widgetconfig;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import java.util.List;

import javax.inject.Inject;

import sw.es.appwidget.ForecastAppWidgetService;
import sw.es.dagger2.R;
import sw.es.dagger2.databinding.ActivityAppwidgetConfigBinding;
import sw.es.di.component.AppWidgetConfigComponent;
import sw.es.di.component.DaggerAppWidgetConfigComponent;
import sw.es.di.module.AppWidgetConfigModule;
import sw.es.domain.recycler.RecyclerSlideInUpAnimator;
import sw.es.model.local.FavouriteLocation;
import sw.es.view.activity.BaseActivity;
import sw.es.view.adapter.AppWidgetConfigAdapter;
import sw.es.view.adapter.event.RecyclerItemClickListener;
import sw.es.view.decorator.SpaceItemDecoration;
import sw.es.viewmodel.appwidgetconfig.AppWidgetConfigViewModel;
import sw.es.viewmodel.appwidgetconfig.FavouriteLocationsCallback;

import static android.util.Log.e;
import static sw.es.dagger2.BuildConfig.DEBUG;

/**
 * Created by albertopenasamor on 16/11/15.
 */
//TODO: no funciona el ripple pq el listener de alguna manera se lo come....poner otro listener o lo que sea
public class AppWidgetConfigActivity extends BaseActivity implements FavouriteLocationsCallback {


    private static final String TAG = AppWidgetConfigActivity.class.getSimpleName();
    @Inject AppWidgetConfigViewModel viewModel;
    private ActivityAppwidgetConfigBinding binding;
    private AppWidgetConfigAdapter adapter;
    private int mAppWidgetId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAsCanceled();

        setContentView(R.layout.activity_appwidget_config);
        getAppWidgetID();
        finishIfInvalidAppWidget();

        setView();
        initViewModel();
    }

    @Override
    protected void initializeInjector() {
        AppWidgetConfigComponent component = DaggerAppWidgetConfigComponent.builder()
                .applicationComponent(getApplicationComponent())
                .appWidgetConfigModule(new AppWidgetConfigModule())
                .build();
        component.inject(AppWidgetConfigActivity.this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.destroy();
    }


    private void finishIfInvalidAppWidget(){
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }


    private void setView() {
        binding = DataBindingUtil.setContentView(AppWidgetConfigActivity.this, R.layout.activity_appwidget_config);
        binding.recycler.setLayoutManager(new LinearLayoutManager(AppWidgetConfigActivity.this));
        SpaceItemDecoration spaceItemDecoration = new SpaceItemDecoration((int) getResources().getDimension(R.dimen.divider_height));
        adapter = new AppWidgetConfigAdapter();
        binding.recycler.setAdapter(adapter);
        binding.recycler.addItemDecoration(spaceItemDecoration);
        binding.recycler.setItemAnimator(new RecyclerSlideInUpAnimator().build());
        binding.recycler.setHasFixedSize(true);//for performance same height
        binding.recycler.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View childView, int position) {
                if (DEBUG) {
                    e(TAG, "onItemClick");
                }
                viewModel.configure(mAppWidgetId, adapter.getFavouriteLocationList().get(position));
            }

            @Override
            public void onItemLongPress(View childView, int position) {}
        }));
        setSupportActionBar(binding.toolbar);
    }


    private void initViewModel() {
        viewModel.setup(this);
        viewModel.load();
    }


    private void setAsCanceled(){
        setResult(RESULT_CANCELED);
    }


    private void getAppWidgetID(){
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
    }


    @Override
    public void onLocations(List<FavouriteLocation> favouriteLocationList) {
        if (DEBUG) {
            e(TAG, "onLocations");
        }
        adapter.setFavouriteLocationList(favouriteLocationList);
    }


    @Override
    public void close() {
        if (DEBUG) {
            e(TAG, "close");
        }
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    @Override
    public void fetchForecast() {
        if (DEBUG) {
            e(TAG, "fetchForecast: " + mAppWidgetId);
        }
        Intent intent = ForecastAppWidgetService.newInstance(AppWidgetConfigActivity.this, mAppWidgetId);
        startService(intent);
    }
}
