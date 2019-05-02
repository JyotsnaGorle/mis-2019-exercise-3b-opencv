package mis.example.misopencv;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.osgi.OpenCVInterface;
import org.opencv.osgi.OpenCVNativeLoader;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase    mOpenCvCameraView;
    private boolean                 mIsJavaCamera = true;
    private MenuItem                mItemSwitchCamera = null;
    Mat tmp;
    CascadeClassifier cascadeClassifier;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    cascadeClassifier = new CascadeClassifier(initAssetFile("haarcascade_frontalface_default.xml"));
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        // before opening the CameraBridge, we need the Camera Permission on newer Android versions

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0x123);
        } else {
            mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
            mOpenCvCameraView.setCvCameraViewListener(this);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        CascadeClassifier frontFace = new CascadeClassifier(initAssetFile("haarcascade_frontalface_default.xml"));

    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        //return inputFrame.rgba();

//        Mat col  = inputFrame.rgba();
//        tmp = col.t();
//        Core.flip(col.t(), tmp, 1);
//        Imgproc.resize(tmp, tmp, col.size());
//        Rect foo = new Rect(new Point(100,100), new Point(200,200));
//        Imgproc.rectangle(tmp, foo.tl(), foo.br(), new Scalar(0, 0, 255), 3);
//        col.release();
//        return tmp;


        Mat gray = inputFrame.gray();
        Mat col  = inputFrame.rgba();

        Mat clone = gray.clone();
//        Imgproc.Canny(gray, clone, 80, 100);
        Imgproc.cvtColor(clone, col, Imgproc.COLOR_GRAY2RGBA, 4);

        tmp = clone.t();
        Core.flip(clone.t(), tmp, 1);
        Imgproc.resize(tmp, tmp, clone.size());
        clone.release();

        MatOfRect rects = new MatOfRect();
        cascadeClassifier.detectMultiScale(tmp, rects,1.1, 3, 3, new Size(5, 5), new Size() );
        Log.d("Rects", rects.toString());
        for(Rect r: rects.toArray()) {
//            Rect foo = new Rect(new Point(100,100), new Point(200,200));
//            Imgproc.rectangle(tmp, r.tl(), r.br(), new Scalar(0, 0, 255), 3);
            Rect foo = new Rect(new Point(100,100), new Point(200,200));
            Imgproc.rectangle(tmp, foo.tl(), foo.br(), new Scalar(0, 0, 255), 3);
        }

        return tmp;
    }


    public String initAssetFile(String filename)  {
        File file = new File(getFilesDir(), filename);
        if (!file.exists()) try {
            InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_default);
//                    getAssets().open(filename);
            File cascadeDir = getDir("cascade", MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, filename);
            OutputStream os = new FileOutputStream(mCascadeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
        } catch (IOException e) { e.printStackTrace(); }
        Log.d(TAG,"prepared local file: "+filename);
        return file.getAbsolutePath();
    }
}
