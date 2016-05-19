package xwang.cordova.vcloud.liveplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.netease.neliveplayer.NELivePlayer;
import com.netease.neliveplayer.NEMediaPlayer;
import java.io.IOException;

public class LivePlayerView extends SurfaceView {
  private static Context mContext;
  private int mVideoWidth;
  private int mVideoHeight;
  private SurfaceHolder mSurfaceHolder;
  private NELivePlayer mMediaPlayer;
  private boolean mIsPrepared;
  private String mUrl;

  private NELivePlayer.OnCompletionListener mOnCompletionListener = null;
  private NELivePlayer.OnErrorListener mOnErrorListener = null;
  private NELivePlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = null;
  private NELivePlayer.OnInfoListener mOnInfoListener = null;
  private View.OnTouchListener mOnTouchListener = null;

  public LivePlayerView(Context context) {
    super(context);
    mContext = context;
    initView();
  }

  public LivePlayerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
    initView();
  }

  public LivePlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    mContext = context;
    initView();
  }

  private void initView() {
    mVideoWidth = 0;
    mVideoHeight = 0;
    getHolder().addCallback(mSHCallback);
    setFocusable(true);
    setFocusableInTouchMode(true);
    requestFocus();
  }

  SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
      mSurfaceHolder = surfaceHolder;
      openVideo();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
      mSurfaceHolder = surfaceHolder;
      if (mMediaPlayer != null) {
        mMediaPlayer.setDisplay(surfaceHolder);
      }
      start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
      mSurfaceHolder = null;
      if (mMediaPlayer != null) {
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;
      }
    }
  };

  public void setVideoPath(String path) {
    mUrl = path;
  }

  private void openVideo() {
    if (mUrl == null || mSurfaceHolder == null) {
      return;
    }
    Intent intent = new Intent("com.android.music.musicservicecommand");
    intent.putExtra("command", "pause");
    mContext.sendBroadcast(intent);

    if (mMediaPlayer != null) {
      mMediaPlayer.reset();
      mMediaPlayer.release();
      mMediaPlayer = null;
    }
    try {
      mIsPrepared = false;
      mMediaPlayer = new NEMediaPlayer();
      mMediaPlayer.setBufferStrategy(0);
      mMediaPlayer.setHardwareDecoder(false);
      mMediaPlayer.setOnPreparedListener(mPreparedListener);
      mMediaPlayer.setOnCompletionListener(mCompletionListener);
      mMediaPlayer.setOnErrorListener(mErrorListener);
      mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
      mMediaPlayer.setOnInfoListener(mInfoListener);

      int ret = mMediaPlayer.setDataSource(mUrl);
      if (ret < 0) {
        if (mMediaPlayer != null) {
          mMediaPlayer.reset();
          mMediaPlayer.release();
          mMediaPlayer = null;
        }
        return;
      }
      mMediaPlayer.setDisplay(mSurfaceHolder);
      mMediaPlayer.setScreenOnWhilePlaying(true);
      mMediaPlayer.prepareAsync(mContext);
    }
    catch (IOException e) {
      mErrorListener.onError(mMediaPlayer, -1, 0);
      return;
    }
    catch (IllegalArgumentException e) {
      mErrorListener.onError(mMediaPlayer, -1, 0);
      return;
    }
  }

  NELivePlayer.OnPreparedListener mPreparedListener = new NELivePlayer.OnPreparedListener() {
    @Override
    public void onPrepared(NELivePlayer mediaPlayer) {
      mVideoWidth = mediaPlayer.getVideoWidth();
      mVideoHeight = mediaPlayer.getVideoHeight();
      mIsPrepared = true;
      start();
    }
  };

  public void setOnCompletionListener(NELivePlayer.OnCompletionListener l) {
    mOnCompletionListener = l;
  }

  NELivePlayer.OnCompletionListener mCompletionListener = new NELivePlayer.OnCompletionListener() {
    @Override
    public void onCompletion(NELivePlayer neLivePlayer) {
      if (mOnCompletionListener != null) {
        mOnCompletionListener.onCompletion(neLivePlayer);
      }
    }
  };

  public void setOnErrorListener(NELivePlayer.OnErrorListener l) {
    mOnErrorListener = l;
  }

  NELivePlayer.OnErrorListener mErrorListener = new NELivePlayer.OnErrorListener() {
    @Override
    public boolean onError(NELivePlayer neLivePlayer, int i, int i1) {
      if (mOnErrorListener != null) {
        return mOnErrorListener.onError(neLivePlayer, i, i1);
      }
      return false;
    }
  };

  public void setOnBufferingUpdateListener(NELivePlayer.OnBufferingUpdateListener l) {
    mOnBufferingUpdateListener = l;
  }

  NELivePlayer.OnBufferingUpdateListener mBufferingUpdateListener = new NELivePlayer.OnBufferingUpdateListener() {
    @Override
    public void onBufferingUpdate(NELivePlayer neLivePlayer, int i) {
      if (mOnBufferingUpdateListener != null) {
        mOnBufferingUpdateListener.onBufferingUpdate(neLivePlayer, i);
      }
    }
  };

  public void setOnInfoListener(NELivePlayer.OnInfoListener l) {
    mOnInfoListener = l;
  }

  NELivePlayer.OnInfoListener mInfoListener = new NELivePlayer.OnInfoListener() {
    @Override
    public boolean onInfo(NELivePlayer neLivePlayer, int i, int i1) {
      if (mOnInfoListener != null) {
        mOnInfoListener.onInfo(neLivePlayer, i, i1);
      }
      return false;
    }
  };


  public void setOnTouchListener(View.OnTouchListener l) {
    mOnTouchListener = l;
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    if (mOnTouchListener != null) {
      return mOnTouchListener.onTouch(this, ev);
    }
    return true;
  }

  public void start() {
    if (mMediaPlayer != null && mIsPrepared) {
      mMediaPlayer.start();
    }
  }

  public void setMute(boolean mute) {
    if (mMediaPlayer != null && mIsPrepared) {
      mMediaPlayer.setMute(mute);
    }
  }

  public void getSnapshot() {
    if (mMediaPlayer != null && mIsPrepared) {
      Bitmap bitmap = Bitmap.createBitmap(mVideoWidth, mVideoHeight, Bitmap.Config.ARGB_8888);
      mMediaPlayer.getSnapshot(bitmap);
      MediaStore.Images.Media.insertImage(mContext.getContentResolver(), bitmap, "snapshot.jpg", null);
      Toast.makeText(mContext, "截图成功", Toast.LENGTH_SHORT).show();
    }
  }
}