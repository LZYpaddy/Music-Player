package cn.itcast.musicplayer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import cn.itcast.musicplayer.R;
import cn.itcast.musicplayer.pojo.Song;
import cn.itcast.musicplayer.service.NetService;

import java.util.List;


public class SongItem extends ArrayAdapter<Song> {
    private Context context;
    private int res;
    private NetService.MyBinder binder;
    private Handler handler;

    public SongItem(Context context, NetService.MyBinder binder, Handler handler, int res, List<Song> list) {
        super( context, res, list );
        this.handler = handler;
        this.binder = binder;
        this.context = context;
        this.res = res;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Song item = getItem( position );
        ViewHolder holder = new ViewHolder();
        if (convertView == null) {
            convertView = LayoutInflater.from( context ).inflate( res, parent, false );
            holder.name = convertView.findViewById( R.id.tv_name );
            holder.downLoad = convertView.findViewById( R.id.bt_download );
            convertView.setTag( holder );
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.name.setText( item.getName() );
        holder.downLoad.setOnClickListener( v -> {
            if (binder != null) {
                binder.downloadSong( handler, item );
            } else {
                Toast.makeText( SongItem.this.getContext(), "null!!!!", Toast.LENGTH_SHORT ).show();
            }
        } );
        return convertView;
    }

    private class ViewHolder {
        TextView name;
        Button downLoad;
    }

}
