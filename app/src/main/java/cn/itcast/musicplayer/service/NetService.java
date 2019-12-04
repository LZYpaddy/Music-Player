package cn.itcast.musicplayer.service;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.support.annotation.Nullable;
import android.util.Log;
import cn.itcast.musicplayer.activity.PlayerActivity;
import cn.itcast.musicplayer.pojo.Song;
import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Arrays.asList;


public class NetService extends Service {
    public MyBinder binder = new MyBinder();
    private String socket = "10.131.163.221:8080";///IP
    ExecutorService pool = Executors.newCachedThreadPool();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public class MyBinder extends Binder {
        public void getSongList(final Handler handler) {
            pool.execute( () -> {
                try {
                    URL url = new URL( "http://"+ socket +"/song/list" );
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod( "GET" );
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append( line );
                    }
                    List<Song> list = asList( new Gson().fromJson( builder.toString(), Song[].class ) );
                    Log.i( "001", builder.toString() );
                    Message message = new Message();
                    message.what = 0;
                    message.obj = list;
                    handler.sendMessage( message );
                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } );
        }

        public void downloadSong(final Handler handler, Song song) {
            pool.execute( () -> {
                try {
                    int id = song.getId();
                    if (null == song.getPath()) {
                        // 请求
                        URL url = new URL( "http://" + socket + "/song/download/" + id );
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod( "GET" );
                        InputStream in = connection.getInputStream();
                        // 构造路径
                        String path = Environment.getExternalStorageDirectory().toString() + "/Music" + "/" + id + ".map3";
                        Log.i( "001", path );
                        // 写入文件
                        File file = new File( path );
                        if (!file.exists()) {
                            file.createNewFile();
                            FileOutputStream out = new FileOutputStream( file );
                            byte[] buf = new byte[1024];
                            int ch;
                            while ((ch = in.read( buf )) != -1) {

                                out.write( buf, 0, ch );
                            }
                            out.flush();
                            out.close();
                        }
                        connection.disconnect();
                        song.setPath( path );
                    }
                    // 发送message
                    Message message = new Message();
                    message.what = 1;
                    message.obj = song;
                    handler.sendMessage( message );

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } );
        }
    }
}
