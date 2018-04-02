package com.redstar.gifttime;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.IOException;
import java.util.List;


public class CameraFragment extends Fragment implements SurfaceHolder.Callback,
        Camera.AutoFocusCallback, Camera.PictureCallback {
    private View view;
    private Camera camera;
    private SurfaceView preview;
    private SurfaceHolder surfaceHolder;
    private FragmentListener mListener;
    private CameraFragment thisFragment;
    private boolean prepare;

    public CameraFragment() {

    }

    /**
     * Creates new {@link CameraFragment} instance.
     *
     * @return new {@link CameraFragment} object
     */
    public static CameraFragment newInstance(String param1, String param2) {
        return new CameraFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisFragment = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_camera, container, false);
        preview = view.findViewById(R.id.cameraSurface);
        /// Set up autofocus when user touch screen
        preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.autoFocus(thisFragment);
            }
        });

        /// Initialize preview surface
        surfaceHolder = preview.getHolder();
        surfaceHolder.addCallback(thisFragment);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        /// Set callback to make photo button
        AppCompatButton btn = view.findViewById(R.id.makePhotoButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepare = true;
                camera.autoFocus(thisFragment);
            }
        });
        return view;
    }

    /**
     * Auto focus callback. Called when camera was focused.
     *
     * @param paramBoolean true, if camera was successfully focused, false otherwise.
     * @param paramCamera Camera object
     */
    @Override
    public void onAutoFocus(boolean paramBoolean, Camera paramCamera) {
        if (paramBoolean) {
            if (prepare) {
                paramCamera.takePicture(null, null, null, this);
            }
        }
        prepare = false;
    }

    /**
     * Called when picture has been taken.
     *
     * @param photo photo converted in byte array
     * @param paramCamera Camera object
     */
    @Override
    public void onPictureTaken(byte[] photo, Camera paramCamera) {
        /// Convert byte array to Bitmap
        Bitmap taken = BitmapFactory.decodeByteArray(photo, 0, photo.length);

        ///Rotate picture on 90 degrees
        Matrix matrix = new Matrix();
        Camera.Parameters cp = camera.getParameters();
        Camera.Size size = cp.getPictureSize();
        matrix.postRotate(90);
        ImageView img = view.findViewById(R.id.cameraOverlayImage);

        /// Crop photo by overlay area
        int px_height = (int) (img.getWidth() * size.height / ((float) preview.getWidth()));
        int px_width = px_height * 5 / 8;
        Bitmap cropped = Bitmap.createBitmap(taken, (size.width - px_width) / 2,
                (size.height - px_height) / 2, px_width, px_height, matrix, false);

        /// Throw cropped photo to activity
        mListener.onPhotoTaken(cropped);
    }

    /**
     * Calls when surface has been created
     *
     * @param holder Preview holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        safeCameraOpen();
    }

    /**
     * Calls when surface parameters has been changed
     *
     * @param holder Preview holder
     * @param format Unknown
     * @param height Height size
     * @param width Width size
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    /**
     * Calls when surface has been destroyed
     *
     * @param holder Preview holder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    /**
     * Opens {@link android.graphics.Camera camera} safety (with releasing another camera if
     * it was opened). Rotates camera and applies optimal picture resolution.
     */
    public void safeCameraOpen() {
        boolean qOpened;

        try {
            releaseCameraAndPreview();
            camera = Camera.open();
            qOpened = (camera != null);
            if (qOpened) {
                try {
                    camera.setDisplayOrientation(90);
                    camera.setPreviewDisplay(surfaceHolder);
                    Camera.Parameters cp = camera.getParameters();
                    List<Camera.Size> sizes = cp.getSupportedPreviewSizes();
                    cp.setPreviewSize(sizes.get(sizes.size() - 1).width, sizes.get(sizes.size() - 1).height);
//                    sizes = cp.getSupportedPictureSizes();
//                    cp.setPictureSize(sizes.get(sizes.size()-1).width, sizes.get(sizes.size()-1).height);
                    camera.setParameters(cp);
                    camera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                    releaseCameraAndPreview();
                }
            } else Log.w("Camera Gift Time", "Failed to open Camera");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Releases camera with preview stopping.
     */
    public void releaseCameraAndPreview() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentListener) {
            mListener = (FragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface FragmentListener {
        /// Method, which throws photo to activity
        void onPhotoTaken(Bitmap photo);
    }
}
