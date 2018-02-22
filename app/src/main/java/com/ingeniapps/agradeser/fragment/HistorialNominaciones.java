package com.ingeniapps.agradeser.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ingeniapps.agradeser.R;
import com.ingeniapps.agradeser.activity.DetalleNominacion;
import com.ingeniapps.agradeser.adapter.VotoAdapter;
import com.ingeniapps.agradeser.beans.Voto;
import com.ingeniapps.agradeser.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.agradeser.util.SimpleDividerItemDecoration;
import com.ingeniapps.agradeser.vars.vars;
import com.ingeniapps.agradeser.volley.ControllerSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistorialNominaciones extends Fragment
{
    private gestionSharedPreferences sharedPreferences;
    private ArrayList<Voto> listadoVotos;
    public vars vars;
    private RecyclerView recycler_view_votos;
    private VotoAdapter mAdapter;
    LinearLayoutManager mLayoutManager;
    LinearLayout linearHabilitarNominaciones;
    RelativeLayout layoutEspera;
    RelativeLayout layoutMacroEsperaVotos;
    ImageView not_found_votos;
    Context context;
    TextView editTextNumNominaciones;
    //VERSION DEL APP INSTALADA
    private String versionActualApp;
    com.ingeniapps.agradeser.sharedPreferences.gestionSharedPreferences gestionSharedPreferences;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        listadoVotos=new ArrayList<Voto>();
        vars=new vars();
        context = getActivity();
        gestionSharedPreferences=new gestionSharedPreferences(HistorialNominaciones.this.getActivity());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        try
        {
            versionActualApp=getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

        viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) getActivity().findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }



    private void setupViewPager(ViewPager viewPager)
    {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new MisNominaciones(), "Mis Nominaciones");
        adapter.addFragment(new MisNominados(), "Mis Nominados");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter
    {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager)
        {
            super(manager);
        }

        @Override
        public Fragment getItem(int position)
        {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount()
        {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title)
        {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.i("fabio","onResume");
        //sharedPreferences.putInt("counterNotificacion",0);
        //WebServiceGetVotos(""+gestionSharedPreferences.getString("codEmpleado"));
        updateTokenFCMToServer();

    }

    @Override
    public void onPause()
    {
        super.onPause();
        Log.i("fabio","onPause");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_historial_nominaciones, container, false);
    }

    private void WebServiceGetVotos(final String codEmpleado)
    {
        listadoVotos.clear();
        String _urlWebService = vars.ipServer.concat("/ws/getVotos");

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, _urlWebService, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            if(response.getBoolean("status"))
                            {
                                layoutMacroEsperaVotos.setVisibility(View.GONE);
                                //recycler_view_votos.setVisibility(View.VISIBLE);


                                JSONArray listaVotos = response.getJSONArray("votos");

                                for (int i = 0; i<listaVotos.length(); i++)
                                {
                                    JSONObject jsonObject = (JSONObject) listaVotos.get(i);
                                    Voto voto = new Voto();
                                    voto.setType(jsonObject.getString("type"));
                                    voto.setFecVoto(jsonObject.getString("timeStampItemVoto"));
                                    voto.setNomEmpleado(jsonObject.getString("nomEmpleado"));
                                    voto.setDesMotivo(jsonObject.getString("desMotivo"));
                                    listadoVotos.add(voto);
                                }

//                                editTextNumNominaciones.setText(""+listaVotos.length()+" Nominaciones");
                                //linearHabilitarNominaciones.setVisibility(View.VISIBLE);
                            }

                            else
                            {
                                layoutMacroEsperaVotos.setVisibility(View.VISIBLE);
                                layoutEspera.setVisibility(View.GONE);
                                not_found_votos.setVisibility(View.VISIBLE);
                            }
                        }
                        catch (JSONException e)
                        {
                            layoutMacroEsperaVotos.setVisibility(View.VISIBLE);
                            layoutEspera.setVisibility(View.GONE);
                            not_found_votos.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(HistorialNominaciones.this.getActivity(),R.style.AlertDialogTheme));
                            builder
                                    .setMessage(e.getMessage().toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {

                                        }
                                    }).show();

                            e.printStackTrace();
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        if (error instanceof TimeoutError)
                        {
                            layoutMacroEsperaVotos.setVisibility(View.VISIBLE);
                            layoutEspera.setVisibility(View.GONE);
                            not_found_votos.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(HistorialNominaciones.this.getActivity(),R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error de conexión, sin respuesta del servidor.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
                        }

                        else

                        if (error instanceof NoConnectionError)
                        {
                            layoutMacroEsperaVotos.setVisibility(View.VISIBLE);
                            layoutEspera.setVisibility(View.GONE);
                            not_found_votos.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(HistorialNominaciones.this.getActivity(),R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Por favor, conectese a la red.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
                        }

                        else

                        if (error instanceof AuthFailureError)
                        {
                            layoutMacroEsperaVotos.setVisibility(View.VISIBLE);
                            layoutEspera.setVisibility(View.GONE);
                            not_found_votos.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(HistorialNominaciones.this.getActivity(),R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error de autentificación en la red, favor contacte a su proveedor de servicios.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
                        }

                        else

                        if (error instanceof ServerError)
                        {
                            layoutMacroEsperaVotos.setVisibility(View.VISIBLE);
                            layoutEspera.setVisibility(View.GONE);
                            not_found_votos.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(HistorialNominaciones.this.getActivity(),R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error server, sin respuesta del servidor.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
                        }

                        else

                        if (error instanceof NetworkError)
                        {
                            layoutMacroEsperaVotos.setVisibility(View.VISIBLE);
                            layoutEspera.setVisibility(View.GONE);
                            not_found_votos.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(HistorialNominaciones.this.getActivity(),R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error de red, contacte a su proveedor de servicios.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
                        }

                        else

                        if (error instanceof ParseError)
                        {
                            layoutMacroEsperaVotos.setVisibility(View.VISIBLE);
                            layoutEspera.setVisibility(View.GONE);
                            not_found_votos.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(HistorialNominaciones.this.getActivity(),R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error de conversión Parser, contacte a su proveedor de servicios.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
                        }
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap <String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                headers.put("codEmpleado", codEmpleado);
                headers.put("tokenFCM", FirebaseInstanceId.getInstance().getToken());
               // headers.put("MyToken", sharedPreferences.getString("MyToken"));
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void updateTokenFCMToServer()
    {
        String _urlWebServiceUpdateToken = vars.ipServer.concat("/ws/UpdateTokenFCM");

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, _urlWebServiceUpdateToken, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            Boolean status = response.getBoolean("status");
                            String message = response.getString("message");

                            if(status)
                            {
                            }
                            else
                            {
                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        //Toast.makeText(getActivity(), "Token FCM: " + "error"+error.getMessage(), Toast.LENGTH_LONG).show();

                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                headers.put("codEmpleado", gestionSharedPreferences.getString("codEmpleado"));
                headers.put("tokenFCM", ""+FirebaseInstanceId.getInstance().getToken());
                return headers;
            }
        };
        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }


}
