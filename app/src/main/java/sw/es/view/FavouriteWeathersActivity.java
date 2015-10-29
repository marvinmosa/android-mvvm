package sw.es.view;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;

import javax.inject.Inject;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import sw.es.dagger2.R;
import sw.es.dagger2.databinding.ActivityHomeBinding;
import sw.es.di.component.DaggerFavouriteWeathersViewModelComponent;
import sw.es.di.component.FavouriteWeathersViewModelComponent;
import sw.es.di.module.FavouriteWeathersViewModelModule;
import sw.es.model.local.FavouriteLocation;
import sw.es.model.local.Weather;
import sw.es.view.adapter.WeatherAdapter;
import sw.es.view.adapter.dragandswipe.SimpleItemTouchHelperCallback;
import sw.es.view.decorator.SpaceItemDecoration;
import sw.es.viewmodel.weather.FavouriteWeathersListener;
import sw.es.viewmodel.weather.FavouriteWeathersViewModel;

import static android.util.Log.e;
import static sw.es.dagger2.BuildConfig.DEBUG;

//TODO: completar row
//TODO: remove weather cuando swipe
//TODO: drag and drop => reorder en shared
//TODO: entrada de weathers temporizados...revisar funcionamient
//TODO: subscripciones en viewmodel
//TODO: widget con forecast
//TODO: repo de forecast? donde integrarlo? hacer solo widget
public class FavouriteWeathersActivity extends BaseActivity implements
        FavouriteWeathersListener,
        SearchView.OnQueryTextListener {


    private static final String TAG = FavouriteWeathersActivity.class.getSimpleName();
    @Inject FavouriteWeathersViewModel viewModel;
    private SearchView searchView;
    private WeatherAdapter adapter;
    private ActivityHomeBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setView();
        initViewModel();
    }


    private void setView() {
        adapter = new WeatherAdapter();
        binding = DataBindingUtil.setContentView(FavouriteWeathersActivity.this, R.layout.activity_home);
        binding.setFavoriteVM(viewModel);
        binding.recycler.setLayoutManager(new LinearLayoutManager(FavouriteWeathersActivity.this));
        SpaceItemDecoration spaceItemDecoration = new SpaceItemDecoration((int) getResources().getDimension(R.dimen.divider_height));
        binding.recycler.addItemDecoration(spaceItemDecoration);
        binding.recycler.setItemAnimator(makeAnimator());
        binding.recycler.setHasFixedSize(true);//for performance same height
        binding.recycler.setAdapter(adapter);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(binding.recycler);
        setSupportActionBar(binding.toolbar);
    }


    private RecyclerView.ItemAnimator makeAnimator(){
        RecyclerView.ItemAnimator animator = new SlideInUpAnimator(new OvershootInterpolator(1f));
        int animMS = 1000;
        animator.setAddDuration(animMS);
        animator.setRemoveDuration(animMS);
        animator.setMoveDuration(animMS);
        animator.setChangeDuration(animMS);
        return animator;
    }


    @Override
    protected void initializeInjector() {
        FavouriteWeathersViewModelComponent component = DaggerFavouriteWeathersViewModelComponent.builder()
                .applicationComponent(getApplicationComponent())
                .favouriteWeathersViewModelModule(new FavouriteWeathersViewModelModule())
                .build();
        component.inject(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_home, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.destroy();
    }


    @Override
    public void onWeather(Weather weather) {
        if (DEBUG) {
            e(TAG, "onWeather");
        }
        adapter.addWeather(weather);
    }


    @Override
    public void onWeatherError(Throwable throwable) {
        if (DEBUG) {
            e(TAG, "onWeatherError: ");
            throwable.printStackTrace();
        }
        Snackbar.make(binding.view, throwable.getMessage()!=null?throwable.getMessage():"Error", Snackbar.LENGTH_LONG).show();
    }


    @Override
    public void alreadyHasFavouriteLocation(FavouriteLocation favouriteLocation) {
        Snackbar.make(binding.view, getResources().getString(R.string.text_already_added_location), Snackbar.LENGTH_LONG).show();
    }


    private void initViewModel() {
        viewModel.setup(this);
        viewModel.load();
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        e("onQueryTextChange", query);
        hideKeyboard();
        viewModel.pull(query);
        return true;
    }


    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }


    private void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null){
            imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
        }
    }
}
