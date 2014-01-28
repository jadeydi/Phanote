package me.idea.phanote;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import me.idea.phanote.service.ClipService;

public class ServiceFragment extends Fragment {

    private Activity mActivity;
    private ImageView mImageView;
    private TextView mTextView;

    public ServiceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.service_fragment, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        mActivity = activity;

        super.onAttach(activity);
    }

    @Override
    public void onStart() {

        mImageView = (ImageView) getView().findViewById(R.id.image_service_status);
        mTextView = (TextView) getView().findViewById(R.id.text_service_status);

        if (ClipService.STATUS == ClipService.STARTED) {
            mImageView.setImageResource(R.drawable.ic_menu_grey_pause);
            mTextView.setText(R.string.stop_clipbroad_listener);
        } else if (ClipService.STATUS == ClipService.STOPED) {
            mImageView.setImageResource(R.drawable.ic_menu_grey_play);
            mTextView.setText(R.string.start_clipbroad_listener);
        }

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ClipService.STATUS == ClipService.STARTED) {
                    mImageView.setImageResource(R.drawable.ic_menu_grey_play);
                    mTextView.setText(R.string.start_clipbroad_listener);
                    mActivity.stopService(new Intent(mActivity, ClipService.class));
                } else if (ClipService.STATUS == ClipService.STOPED) {
                    mImageView.setImageResource(R.drawable.ic_menu_grey_pause);
                    mTextView.setText(R.string.stop_clipbroad_listener);
                    mActivity.startService(new Intent(mActivity, ClipService.class));
                }

            }
        });

        super.onStart();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
