package com.ingeniapps.agradeser.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ingeniapps.agradeser.R;
import com.ingeniapps.agradeser.beans.Empleado;
import com.ingeniapps.agradeser.beans.Voto;
import com.ingeniapps.agradeser.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.agradeser.util.MyAnimationUtils;
import com.ingeniapps.agradeser.vars.vars;

import java.util.ArrayList;
import java.util.List;

public class VotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private Activity activity;
    private LayoutInflater inflater;
    private List<Voto> listadoVotos;

    public final int TYPE_NOTICIA=0;
    public final int TYPE_LOAD=1;
    private gestionSharedPreferences sharedPreferences;
    private Context context;
    OnLoadMoreListener loadMoreListener;
    boolean isLoading=false, isMoreDataAvailable=true;
    vars vars;
    int previousPosition=0;


    public interface OnItemClickListener
    {
        void onItemClick(Voto voto);
    }

    private final VotoAdapter.OnItemClickListener listener;

    public VotoAdapter(Activity activity, ArrayList<Voto> listadoVotos, VotoAdapter.OnItemClickListener listener)
    {
        this.activity=activity;
        this.listadoVotos=listadoVotos;
        vars=new vars();
        sharedPreferences=new gestionSharedPreferences(this.activity);
        this.listener=listener;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if(viewType==TYPE_NOTICIA)
        {
            return new VotoHolder(inflater.inflate(R.layout.voto_row_layout,parent,false));
        }
        else
        {
            return new LoadHolder(inflater.inflate(R.layout.voto_row_layout,parent,false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        if(position >= getItemCount()-1 && isMoreDataAvailable && !isLoading && loadMoreListener!=null)
        {
            isLoading = true;
            loadMoreListener.onLoadMore();
        }

        if(getItemViewType(position)==TYPE_NOTICIA)
        {
            ((VotoHolder)holder).bindData(listadoVotos.get(position));
        }
/*
        if(position>previousPosition)
        {
            new MyAnimationUtils().animate(holder,true);
        }
        else
        {
            new MyAnimationUtils().animate(holder,false);
        }*/

        previousPosition=position;

    }

    @Override
    public int getItemViewType(int position)
    {

       if(listadoVotos.get(position).getType().equals("voto"))
        {
            Log.i("TYPE","NOTICIA");
            return TYPE_NOTICIA;
        }
        else
        {
            Log.i("TYPE","LOAD");
            return TYPE_LOAD;
        }
    }

    @Override
    public int getItemCount()
    {
        return listadoVotos.size();
    }

    public class VotoHolder extends RecyclerView.ViewHolder
    {
        public TextView nombreVotador;
        public TextView fechaVotacionVotador;

        public VotoHolder(View view)
        {
            super(view);
            nombreVotador=(TextView) view.findViewById(R.id.nombreVotador);
            fechaVotacionVotador=(TextView) view.findViewById(R.id.fechaVotacionVotador);
        }

        void bindData(final Voto voto)
        {
            nombreVotador.setText(""+voto.getNomEmpleado());

            long timestamp = Long.parseLong(voto.getFecVoto()) * 1000L;
            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(timestamp,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
            fechaVotacionVotador.setText(""+timeAgo);

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override public void onClick(View v)
                {
                    listener.onItemClick(voto);
                }
            });
        }
    }

    static class LoadHolder extends RecyclerView.ViewHolder
    {
        public LoadHolder(View itemView)
        {
            super(itemView);
        }
    }

    public void setMoreDataAvailable(boolean moreDataAvailable)
    {
        isMoreDataAvailable = moreDataAvailable;
    }
    /* notifyDataSetChanged is final method so we can't override it
        call adapter.notifyDataChanged(); after update the list
        */
    public void notifyDataChanged()
    {
        notifyDataSetChanged();
        isLoading = false;
    }

    public interface OnLoadMoreListener
    {
        void onLoadMore();
    }

    public void setLoadMoreListener(OnLoadMoreListener loadMoreListener)
    {
        this.loadMoreListener = loadMoreListener;
    }

    public List<Voto> getNoticiasList()
    {
        return listadoVotos;
    }

}
