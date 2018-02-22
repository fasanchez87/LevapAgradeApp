package com.ingeniapps.agradeser.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.ingeniapps.agradeser.adapter.EmpleadoAdapter;
import com.ingeniapps.agradeser.beans.Empleado;
import com.ingeniapps.agradeser.fragment.Nominar;
import com.ingeniapps.agradeser.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.agradeser.util.SimpleDividerItemDecoration;
import com.ingeniapps.agradeser.vars.vars;
import com.ingeniapps.agradeser.volley.ControllerSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.ingeniapps.agradeser.vars.vars;

public class Buscar extends AppCompatActivity
{

    private gestionSharedPreferences sharedPreferences;
    private ArrayList<Empleado> listadoEmpleado;
    private RecyclerView recycler_view_empleados;
    private EmpleadoAdapter mAdapter;
    LinearLayoutManager mLayoutManager;

    LinearLayout linearHabilitarEmpleados;
    RelativeLayout layoutEspera;
    RelativeLayout layoutMacroEsperaEmpleados;

    private int pagina;
    Context context;
    private boolean solicitando=false;
    //VERSION DEL APP INSTALADA
    private String versionActualApp;
    private ImageView not_found_empleados;

    private ProgressDialog progressDialog;

    EditText editTextBusquedaCompañero;
    TextView editTextNumEmpleados;
    private String idCategoria;

    DividerItemDecoration mDividerItemDecoration;

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    vars vars;
    com.ingeniapps.agradeser.sharedPreferences.gestionSharedPreferences gestionSharedPreferences;




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar);
        vars=new vars();
        gestionSharedPreferences=new gestionSharedPreferences(Buscar.this);


        progressDialog = new ProgressDialog(new android.support.v7.view.ContextThemeWrapper(Buscar.this,R.style.AppCompatAlertDialogStyle));
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Consultando empleados, un momento...");

        sharedPreferences=new gestionSharedPreferences(this);
        listadoEmpleado=new ArrayList<Empleado>();
        vars=new vars();
        context = this;
        pagina=0;

        editTextNumEmpleados=(TextView)findViewById(R.id.editTextNumEmpleados);
        editTextBusquedaCompañero=(EditText)findViewById(R.id.editTextBusquedaCompañero);

        not_found_empleados=(ImageView)findViewById(R.id.not_found_empleados);
        layoutEspera=(RelativeLayout)findViewById(R.id.layoutEsperaEmpleados);
        layoutMacroEsperaEmpleados=(RelativeLayout)findViewById(R.id.layoutMacroEsperaEmpleados);
        linearHabilitarEmpleados=(LinearLayout)findViewById(R.id.linearHabilitarEmpleados);


        recycler_view_empleados=(RecyclerView) findViewById(R.id.recycler_view_empleados);
        mLayoutManager = new LinearLayoutManager(this);

        mAdapter = new EmpleadoAdapter(this,listadoEmpleado,new EmpleadoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Empleado empleado)
            {
                Intent i=new Intent(Buscar.this, Principal.class);
                i.putExtra("codEmpleado",empleado.getCodEmpleado());
                i.putExtra("nomEmpleado",empleado.getNomEmpleado());
                i.putExtra("codJefe",empleado.getCodJefeEmpleado());
                i.putExtra("nomJefe",empleado.getNomJefeEmpleado());
                startActivity(i);
                finish();
            }
        });

        recycler_view_empleados.setHasFixedSize(true);
        recycler_view_empleados.setLayoutManager(mLayoutManager);
        recycler_view_empleados.setItemAnimator(new DefaultItemAnimator());
        recycler_view_empleados.setAdapter(mAdapter);

        ImageView buttonBuscar = (ImageView) findViewById(R.id.ivSearch);
        buttonBuscar.setClickable(true);
        buttonBuscar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                progressDialog.show();
                progressDialog.setCancelable(false);

                /*if(!TextUtils.isEmpty(editTextBusqueda.getText()))
                {*/
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                //OCULTAMOS TECLADO
                imm.hideSoftInputFromWindow(editTextBusquedaCompañero.getWindowToken(), 0);
                WebServiceGetEmpleados(editTextBusquedaCompañero.getText().toString(),null);
               /* }*/
               /* else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this,R.style.AlertDialogTheme));
                    builder
                            .setTitle("Dicmax")
                            .setMessage("Por favor, ingrese un criterio de busqueda")
                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int id)
                                {

                                }
                            }).show();
                }*/
            }
        });

        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if (extras == null)
            {
                idCategoria = null;
            }
            else
            {
                idCategoria = extras.getString("idCategoria");
            }
        }

        WebServiceGetEmpleados(null,null);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        updateTokenFCMToServer();
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
                headers.put("tokenFCM", ""+ FirebaseInstanceId.getInstance().getToken());
                return headers;
            }
        };
        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }


    @Override
    public void onBackPressed()
    {
        Intent i=new Intent(Buscar.this, Principal.class);
        i.putExtra("codEmpleado","");
        i.putExtra("nomEmpleado","");
        i.putExtra("codJefe","");
        i.putExtra("nomJefe","");
        startActivity(i);
        finish();
    }

    private void WebServiceGetEmpleados(final String busqueda, final String codEmpleado)
    {

        String _urlWebService = vars.ipServer.concat("/ws/getEmpleados");

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
                                listadoEmpleado.clear();
                                layoutMacroEsperaEmpleados.setVisibility(View.GONE);
                                linearHabilitarEmpleados.setVisibility(View.VISIBLE);

                                JSONArray listaEmpleados = response.getJSONArray("empleados");

                                for (int i = 0; i < listaEmpleados.length(); i++)
                                {
                                    JSONObject jsonObject = (JSONObject) listaEmpleados.get(i);
                                    Empleado empleado = new Empleado();
                                    empleado.setCodEmpleado(jsonObject.getString("codEmpleado"));
                                    empleado.setType(jsonObject.getString("type"));
                                    empleado.setCodJefeEmpleado(jsonObject.getString("codJefe"));
                                    empleado.setNomEmpleado(jsonObject.getString("nomEmpleado"));
                                    empleado.setNomJefeEmpleado(jsonObject.getString("nomJefe"));

                                    editTextNumEmpleados.setText(listaEmpleados.length()+" Empleados Encontados");
                                    listadoEmpleado.add(empleado);
                                }

                                if(progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();
                                }
                            }

                            else

                            if(TextUtils.equals(""+response.getString("error").toString(),"sinresult"))
                            {
                                progressDialog.dismiss();
                                listadoEmpleado.clear();
                                editTextNumEmpleados.setText("No se encontraron colaboradores");
                                mAdapter.notifyDataSetChanged();
                                Snackbar.make(findViewById(android.R.id.content),
                                        response.getString("message"), Snackbar.LENGTH_LONG).show();

                            }

                            else

                            if(TextUtils.equals(""+response.getString("error").toString(),"errorminbuscar"))
                            {
                                progressDialog.dismiss();
                                editTextNumEmpleados.setText(""+response.getString("message"));
                                Snackbar.make(findViewById(android.R.id.content),
                                        response.getString("message"), Snackbar.LENGTH_LONG).show();

                            }
                        }
                        catch (JSONException e)
                        {
                            layoutMacroEsperaEmpleados.setVisibility(View.VISIBLE);
                            layoutEspera.setVisibility(View.GONE);
                            not_found_empleados.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this,R.style.AlertDialogTheme));
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
                            layoutMacroEsperaEmpleados.setVisibility(View.VISIBLE);
                            layoutEspera.setVisibility(View.GONE);
                            not_found_empleados.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this,R.style.AlertDialogTheme));
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
                            layoutMacroEsperaEmpleados.setVisibility(View.VISIBLE);
                            layoutEspera.setVisibility(View.GONE);
                            not_found_empleados.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this,R.style.AlertDialogTheme));
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
                            layoutMacroEsperaEmpleados.setVisibility(View.VISIBLE);
                            layoutEspera.setVisibility(View.GONE);
                            not_found_empleados.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this,R.style.AlertDialogTheme));
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
                            layoutMacroEsperaEmpleados.setVisibility(View.VISIBLE);
                            layoutEspera.setVisibility(View.GONE);
                            not_found_empleados.setVisibility(View.VISIBLE);
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this,R.style.AlertDialogTheme));
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
                            layoutMacroEsperaEmpleados.setVisibility(View.VISIBLE);
                            layoutEspera.setVisibility(View.GONE);
                            not_found_empleados.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this,R.style.AlertDialogTheme));
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
                            layoutMacroEsperaEmpleados.setVisibility(View.VISIBLE);
                            layoutEspera.setVisibility(View.GONE);
                            not_found_empleados.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this,R.style.AlertDialogTheme));
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
                headers.put("buscar", TextUtils.isEmpty(busqueda)?"":busqueda);
                headers.put("categoria", TextUtils.isEmpty(codEmpleado)?"":codEmpleado);
                headers.put("MyToken", gestionSharedPreferences.getString("MyToken"));
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

}


