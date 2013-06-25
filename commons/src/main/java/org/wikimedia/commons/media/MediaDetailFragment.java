package org.wikimedia.commons.media;

import android.graphics.*;
import android.os.*;
import android.text.*;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.actionbarsherlock.app.SherlockFragment;
import com.android.volley.toolbox.NetworkImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import com.android.volley.toolbox.*;

import org.mediawiki.api.ApiResult;
import org.mediawiki.api.MWApi;
import org.wikimedia.commons.*;

import java.io.IOException;
import java.util.ArrayList;

public class MediaDetailFragment extends SherlockFragment {

    private boolean editable;
    private DisplayImageOptions displayOptions;
    private MediaDetailPagerFragment.MediaDetailProvider detailProvider;
    private int index;

    public static MediaDetailFragment forMedia(int index) {
        return forMedia(index, false);
    }

    public static MediaDetailFragment forMedia(int index, boolean editable) {
        MediaDetailFragment mf = new MediaDetailFragment();

        Bundle state = new Bundle();
        state.putBoolean("editable", editable);
        state.putInt("index", index);

        mf.setArguments(state);

        return mf;
    }

    private ImageView image;
    //private EditText title;
    private ProgressBar loadingProgress;
    private ImageView loadingFailed;
    private TextView source;
    private TextView license;
    private TextView desc;
    private LinearLayout categoryList;
    private ArrayList<String> categoryNames;


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("index", index);
        outState.putBoolean("editable", editable);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        detailProvider = (MediaDetailPagerFragment.MediaDetailProvider)getActivity();

        if(savedInstanceState != null) {
            editable = savedInstanceState.getBoolean("editable");
            index = savedInstanceState.getInt("index");
        } else {
            editable = getArguments().getBoolean("editable");
            index = getArguments().getInt("index");
        }
        final Media media = detailProvider.getMediaAtPosition(index);

        View view = inflater.inflate(R.layout.fragment_media_detail, container, false);
        image = (ImageView) view.findViewById(R.id.mediaDetailImage);
        //title = (EditText) view.findViewById(R.id.mediaDetailTitle);
        loadingProgress = (ProgressBar) view.findViewById(R.id.mediaDetailImageLoading);
        loadingFailed = (ImageView) view.findViewById(R.id.mediaDetailImageFailed);
        desc = (TextView) view.findViewById(R.id.mediaDetailDesc);
        source = (TextView) view.findViewById(R.id.mediaDetailSource);
        license = (TextView) view.findViewById(R.id.mediaDetailLicense);
        categoryList = (LinearLayout) view.findViewById(R.id.mediaDetailCategoryList);
        categoryNames = new ArrayList<String>();

        // Enable or disable editing on the title
        /*
        title.setClickable(editable);
        title.setFocusable(editable);
        title.setCursorVisible(editable);
        title.setFocusableInTouchMode(editable);
        if(!editable) {
            title.setBackgroundDrawable(null);
        }
        */

        String actualUrl = TextUtils.isEmpty(media.getImageUrl()) ? media.getLocalUri().toString() : media.getThumbnailUrl(640);
        if(actualUrl.startsWith("http")) {
            ImageLoader loader = ((CommonsApplication)getActivity().getApplicationContext()).getImageLoader();
            MediaWikiImageView mwImage = (MediaWikiImageView)image;
            mwImage.setLoadingView(loadingProgress); //FIXME: Set this as an attribute
            mwImage.setMedia(media, loader);
            Log.d("Volley", actualUrl);
            // FIXME: For transparent images

            // Load image metadata: desc, license, categories
            // FIXME: keep the spinner going while we load data
            // FIXME: cache this data
            Utils.executeAsyncTask(new AsyncTask<Void, Void, Void>() {
                private ArrayList<String> cats;

                @Override
                protected Void doInBackground(Void... voids) {
                    cats = new ArrayList<String>();
                    MWApi api = CommonsApplication.createMWApi();
                    try {
                        ApiResult result = api.action("query")
                                .param("prop", "revisions|categories")
                                .param("titles", media.getFilename())
                                .param("rvprop", "content")
                                .param("rvlimit", 1)
                                .param("rvgeneratexml", 1)
                                .get();

                        String wikiSource = result.getString("/api/query/pages/page/revisions/rev");
                        String parseTreeXmlSource = result.getString("/api/query/pages/page/revisions/rev/@parsetree");

                        Log.d("Commons", "media title: " + media.getFilename());
                        Log.d("Commons", "wiki: " + wikiSource);
                        Log.d("Commons", "xml: " + parseTreeXmlSource);

                        for (ApiResult cl : result.getNodes("/api/query/pages/page/categories/cl")) {
                            cats.add(cl.getString("@title"));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    categoryNames = cats;
                    categoryList.removeAllViews();
                    for (String name : categoryNames) {
                        TextView view = (TextView)getActivity().getLayoutInflater().inflate(R.layout.detail_category_item, null, false);
                        view.setText(name);
                        categoryList.addView(view);
                    }
                }
            });
        } else {
            com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(actualUrl, image, displayOptions, new ImageLoadingListener() {
                public void onLoadingStarted(String s, View view) {
                    loadingProgress.setVisibility(View.VISIBLE);
                }

                public void onLoadingFailed(String s, View view, FailReason failReason) {
                    loadingProgress.setVisibility(View.GONE);
                    loadingFailed.setVisibility(View.VISIBLE);
                }

                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    loadingProgress.setVisibility(View.GONE);
                    loadingFailed.setVisibility(View.GONE);
                    image.setVisibility(View.VISIBLE);
                    if(bitmap.hasAlpha()) {
                        image.setBackgroundResource(android.R.color.white);
                    }
                }

                public void onLoadingCancelled(String s, View view) {
                    throw new RuntimeException("Image loading cancelled. But why?");
                }
            });
        }

        desc.setText(media.getDisplayTitle());
        /*
        title.setText(media.getDisplayTitle());

        title.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                detailProvider.getMediaAtPosition(index).setFilename(title.getText().toString());
                detailProvider.getMediaAtPosition(index).setTag("isDirty", true);
                detailProvider.notifyDatasetChanged();
            }

            public void afterTextChanged(Editable editable) {

            }
        });
        */
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        displayOptions = Utils.getGenericDisplayOptions().build();
    }
}
