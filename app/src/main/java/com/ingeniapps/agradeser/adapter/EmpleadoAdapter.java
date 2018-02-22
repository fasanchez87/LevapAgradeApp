package com.ingeniapps.agradeser.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.ingeniapps.agradeser.R;
import com.ingeniapps.agradeser.beans.Empleado;
import com.ingeniapps.agradeser.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.agradeser.vars.vars;
import com.ingeniapps.agradeser.volley.ControllerSingleton;

import java.util.ArrayList;
import java.util.List;

public class EmpleadoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private Activity activity;
    private LayoutInflater inflater;
    private List<Empleado> listadoEmpleado;

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
        void onItemClick(Empleado empleado);
    }

    private final EmpleadoAdapter.OnItemClickListener listener;

    public EmpleadoAdapter(Activity activity, ArrayList<Empleado> listadoEmpleado, EmpleadoAdapter.OnItemClickListener listener)
    {
        this.activity=activity;
        this.listadoEmpleado=listadoEmpleado;
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
            return new EmpleadoHolder(inflater.inflate(R.layout.empleado_row_layout,parent,false));
        }
        else
        {
            return new LoadHolder(inflater.inflate(R.layout.empleado_row_layout,parent,false));
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
            ((EmpleadoHolder)holder).bindData(listadoEmpleado.get(position));
        }

       /* if(position>previousPosition)
        {
            new MyAnimationUtils().animate(holder,true);
        }
        else
        {
            new MyAnimationUtils().animate(holder,false);
        }

        previousPosition=position;*/

    }

    @Override
    public int getItemViewType(int position)
    {

       if(listadoEmpleado.get(position).getType().equals("empleado"))
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
        return listadoEmpleado.size();
    }

    public class EmpleadoHolder extends RecyclerView.ViewHolder
    {
        public TextView nombreEmpleado;
        public TextView nombreJefeEmpleado;

        public EmpleadoHolder(View view)
        {
            super(view);
            nombreEmpleado=(TextView) view.findViewById(R.id.nombreEmpleado);
            nombreJefeEmpleado=(TextView) view.findViewById(R.id.nombreJefeEmpleado);
        }

        void bindData(final Empleado empleado)
        {
            nombreEmpleado.setText(empleado.getNomEmpleado());
            nombreJefeEmpleado.setText(empleado.getNomJefeEmpleado());

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override public void onClick(View v)
                {
                    listener.onItemClick(empleado);
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

    public List<Empleado> getNoticiasList()
    {
        return listadoEmpleado;
    }

}
