package cn.itcast.musicplayer.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;
import cn.itcast.musicplayer.R;
import cn.itcast.musicplayer.adapter.SongItem;
import cn.itcast.musicplayer.pojo.Song;
import cn.itcast.musicplayer.service.NetService;

import java.io.File;
import java.util.LinkedList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements ServiceConnection{
    private NetService.MyBinder binder;
    private ListView lsSong;
    private List<Song> list = new LinkedList<>();
    private SongItem songItem;
    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    list = (List<Song>) msg.obj;
                    songItem.clear();
                    songItem.addAll( list );
                    songItem.notifyDataSetChanged();
                    break;
                case 1:
                    Song song = (Song) msg.obj;
                    for (int i = 0; i<list.size();i++) {
                        if (list.get( i ).getId() == song.getId()) {
                            list.set( i,song );
                        }
                    }
                    Toast.makeText( MainActivity.this, "下载完成", Toast.LENGTH_SHORT ).show();
                    // 跳转到播放界面
                    Intent intent = new Intent( MainActivity.this, PlayerActivity.class );
                    intent.putExtra( "song", song );
                    startActivity( intent );
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_main );
        bindService();
        requestPermission( MainActivity.this );
    }

    private void initView() {
        lsSong = findViewById( R.id.ls_song );
        songItem = new SongItem( this, binder, handler, R.layout.item_song, list );
        lsSong.setAdapter( songItem );
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder iBinder) {
        binder = (NetService.MyBinder) iBinder;
        initView();
        binder.getSongList( handler );
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra( "message" );
        }
    }

    /**
     * 动态注册广播
     */
    private void doRegisterReceiver() {
        MyReceiver chatMessageReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter( "cn.itcast.musicplayer.activity.MainActivity");
        registerReceiver( chatMessageReceiver, filter );
    }

    /**
     * 绑定服务
     */
    private void bindService() {
        Intent bindIntent = new Intent( MainActivity.this, NetService.class );
        boolean b = bindService( bindIntent, this, BIND_AUTO_CREATE );
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this);
    }

    //设备API大于6.0时，主动申请权限
    private void requestPermission(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission( context, Manifest.permission.WRITE_EXTERNAL_STORAGE )
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions( context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, 0 );

            }
        }
    }
}
