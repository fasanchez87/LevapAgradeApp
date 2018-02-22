package com.ingeniapps.agradeser.fragment;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.ingeniapps.agradeser.activity.Buscar;
import com.ingeniapps.agradeser.activity.Principal;
import com.ingeniapps.agradeser.beans.Empleado;
import com.ingeniapps.agradeser.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.agradeser.volley.ControllerSingleton;

import com.ingeniapps.agradeser.vars.vars;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;

import com.ingeniapps.agradeser.vars.vars;


/**
 * A simple {@link Fragment} subclass.
 */
public class Nominar extends Fragment
{
    EditText editTextCompañeroNominar,editTextJefeNominado,editTextMotivoNominacion;
    Button buttonNominarCompañeroEnable;
    Button buttonNominarCompañeroDisable;
    private String codEmpleado;
    private String nomEmpleado;
    private String codJefe;
    private String nomJefe;
    private ProgressDialog progressDialog;
    vars vars;
    com.ingeniapps.agradeser.sharedPreferences.gestionSharedPreferences gestionSharedPreferences;



    public Nominar()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        /*sharedPreferences=new gestionSharedPreferences(getActivity().getApplicationContext());
        listadoCategorias=new ArrayList<Categoria>();
        vars=new vars();
        context = getActivity();
        pagina=0;*/

        gestionSharedPreferences=new gestionSharedPreferences(Nominar.this.getActivity());


        getActivity().getWindow().setSoftInputMode(SOFT_INPUT_ADJUST_PAN);

        vars=new vars();

        if (savedInstanceState == null)
        {
            Bundle extras = getActivity().getIntent().getExtras();
            if (extras == null)
            {
                codEmpleado = null;
                nomEmpleado = null;
                codJefe = null;
                nomJefe = null;
            }
            else
            {
                codEmpleado = extras.getString("codEmpleado");
                nomEmpleado = extras.getString("nomEmpleado");
                codJefe = extras.getString("codJefe");
                nomJefe = extras.getString("nomJefe");
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_nominar, container, false);
    }

    @Override
        public void onViewCreated(View view, Bundle savedInstanceState)
        {
            super.onViewCreated(view, savedInstanceState);

        editTextCompañeroNominar=(EditText) getActivity().findViewById(R.id.editTextCompañeroNominar);
        editTextCompañeroNominar.setText(TextUtils.isEmpty(nomEmpleado)?null:nomEmpleado);
        editTextCompañeroNominar.setInputType(InputType.TYPE_NULL);
        editTextCompañeroNominar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i=new Intent(Nominar.this.getActivity(),Buscar.class);
                startActivity(i);
                getActivity().finish();
            }
        });


        editTextJefeNominado=(EditText) getActivity().findViewById(R.id.editTextJefeNominado);
        editTextJefeNominado.setEnabled(false);
        editTextJefeNominado.setText(TextUtils.isEmpty(nomJefe)?null:nomJefe);

        editTextMotivoNominacion=(EditText) getActivity().findViewById(R.id.editTextMotivoNominacion);
        editTextMotivoNominacion.setEnabled(false);

        buttonNominarCompañeroDisable=(Button) getActivity().findViewById(R.id.buttonNominarCompañeroDisable);
        buttonNominarCompañeroDisable.setEnabled(false);

        buttonNominarCompañeroEnable=(Button) getActivity().findViewById(R.id.buttonNominarCompañeroEnable);

        if(!TextUtils.isEmpty(editTextCompañeroNominar.getText()) && !TextUtils.isEmpty(editTextJefeNominado.getText()))
        {
            editTextMotivoNominacion.setEnabled(true);
            editTextMotivoNominacion.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    if(s.toString().trim().length()==0)
                    {
                        buttonNominarCompañeroDisable.setVisibility(View.VISIBLE);
                        buttonNominarCompañeroEnable.setVisibility(View.GONE);
                    }
                    else
                    {
                        buttonNominarCompañeroDisable.setVisibility(View.GONE);
                        buttonNominarCompañeroEnable.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after)
                {
                    // TODO Auto-generated method stub
                }

                @Override
                public void afterTextChanged(Editable s)
                {
                    // TODO Auto-generated method stub
                }
            });
        }

        buttonNominarCompañeroEnable.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                Log.i("martin",gestionSharedPreferences.getString("codEmpleado"));
                Log.i("martin",codEmpleado);


                String codVotador=gestionSharedPreferences.getString("codEmpleado");//CODIGO DEL VOTANTE.
                String desMotivo=editTextMotivoNominacion.getText().toString();
                WebServiceRegistroVoto(codVotador,codEmpleado,desMotivo);
            }
        });

    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        updateTokenFCMToServer();
    }

    public void inhabilitarComponentes()
    {
        editTextCompañeroNominar.setText(null);
        editTextJefeNominado.setText(null);
        editTextMotivoNominacion.setEnabled(false);
        editTextMotivoNominacion.setText(null);
    }

    private void WebServiceRegistroVoto(final String codVotador, final String codEmpleado, final String desMotivo)
    {
        progressDialog = new ProgressDialog(new android.support.v7.view.ContextThemeWrapper(Nominar.this.getActivity(),R.style.AppCompatAlertDialogStyle));
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Registrando tu voto, espera un momento...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        String _urlWebService = vars.ipServer.concat("/ws/RegistrarVoto");

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, _urlWebService, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            if(response.getBoolean("status"))
                            {
                                progressDialog.dismiss();

                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Nominar.this.getActivity(),R.style.AlertDialogTheme));
                                builder
                                        .setTitle("AgradeSER")
                                        .setMessage(""+response.getString("message"))
                                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id)
                                            {
                                                inhabilitarComponentes();
                                            }
                                        }).setCancelable(false).show();
                            }

                            else
                            {
                                progressDialog.dismiss();



                                Snackbar.make(getActivity().findViewById(android.R.id.content),
                                        "Error registrando tu voto, intenta de nuevo o comunícate con el área encargada", Snackbar.LENGTH_LONG).show();
                            }
                        }
                        catch (JSONException e)
                        {

                            progressDialog.dismiss();


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Nominar.this.getActivity(),R.style.AlertDialogTheme));
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
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        if (error instanceof TimeoutError)
                        {
                            progressDialog.dismiss();

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Nominar.this.getActivity(),R.style.AlertDialogTheme));
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
                            progressDialog.dismiss();

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Nominar.this.getActivity(),R.style.AlertDialogTheme));
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

                            progressDialog.dismiss();

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Nominar.this.getActivity(),R.style.AlertDialogTheme));
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
                            progressDialog.dismiss();

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Nominar.this.getActivity(),R.style.AlertDialogTheme));
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
                            progressDialog.dismiss();

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Nominar.this.getActivity(),R.style.AlertDialogTheme));
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

                            progressDialog.dismiss();

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Nominar.this.getActivity(),R.style.AlertDialogTheme));
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
                headers.put("codVotador", codVotador);
                headers.put("codEmpleado", codEmpleado);
                headers.put("desMotivo", desMotivo);
                headers.put("MyToken", gestionSharedPreferences.getString("MyToken"));
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
