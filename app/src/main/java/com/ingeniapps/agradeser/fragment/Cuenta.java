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
import com.ingeniapps.agradeser.activity.Buscar;
import com.ingeniapps.agradeser.activity.CambioClave;
import com.ingeniapps.agradeser.activity.Login;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class Cuenta extends Fragment
{
    EditText nombreCuenta,emailCuenta,claveCuenta;
    TextView textViewCambioClave;
    Button buttonGuardarDisable;
    Button buttonGuardarCambios;
    private String codEmpleado;
    private String nomEmpleado;
    private String codJefe;
    private String nomJefe;
    private ProgressDialog progressDialog;
    vars vars;
    com.ingeniapps.agradeser.sharedPreferences.gestionSharedPreferences gestionSharedPreferences;


    public Cuenta()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        gestionSharedPreferences=new gestionSharedPreferences(getActivity().getApplicationContext());
        getActivity().getWindow().setSoftInputMode(SOFT_INPUT_ADJUST_PAN);
        vars=new vars();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_cuenta, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        nombreCuenta=(EditText) getActivity().findViewById(R.id.nombreCuenta);
        emailCuenta=(EditText) getActivity().findViewById(R.id.emailCuenta);
        //claveCuenta=(EditText) getActivity().findViewById(R.id.claveCuenta);
        textViewCambioClave=(TextView) getActivity().findViewById(R.id.textViewCambioClave);
        textViewCambioClave.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(Cuenta.this.getActivity(), CambioClave.class);
                startActivity(intent);
            }
        });
        buttonGuardarCambios=(Button) getActivity().findViewById(R.id.buttonGuardarCambios);
        buttonGuardarDisable=(Button) getActivity().findViewById(R.id.buttonGuardarDisable);

        buttonGuardarCambios.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String nombre=nombreCuenta.getText().toString();
                String email=emailCuenta.getText().toString();

                if (TextUtils.isEmpty(nombreCuenta.getText().toString()))
                {
                    Snackbar.make(getActivity().findViewById(android.R.id.content),
                            "Digite su nombre completo, por favor...", Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (TextUtils.isEmpty(email)||!(isValidEmail(email)))
                {
                    Snackbar.make(getActivity().findViewById(android.R.id.content),
                            "Digite un email valido, por favor...", Snackbar.LENGTH_LONG).show();
                    return;
                }

                WebServiceRegistroCambiosCuenta(nombre,email);
        }
        });


        WebServiceGetInfo();
    }

    public final static boolean isValidEmail(CharSequence target)
    {
        if (target == null)
        {
            return false;
        }
        else
        {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
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

    private void WebServiceGetInfo()
    {
        String _urlWebService=vars.ipServer.concat("/ws/GetInfoColaborador");

        progressDialog = new ProgressDialog(new ContextThemeWrapper(Cuenta.this.getActivity(),R.style.AppCompatAlertDialogStyle));
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Cargando información, espera un momento...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        JsonObjectRequest jsonObjReq=new JsonObjectRequest(Request.Method.GET, _urlWebService, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            boolean status=response.getBoolean("status");

                            if(status)
                            {
                                progressDialog.dismiss();
                                nombreCuenta.setText(""+response.getJSONObject("data").getString("nomEmpleado"));
                                emailCuenta.setText(""+response.getJSONObject("data").getString("corEmpleado"));
                            }

                            else
                            //if(sesionAbierta)
                            {
                                progressDialog.dismiss();
                                progressDialog.dismiss();
                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Cuenta.this.getActivity(),R.style.AlertDialogTheme));
                                builder
                                        .setTitle("ESTADO PROCESO")
                                        .setMessage("Error al consultar sus datos, contacte s su Jefe e informe de la novedad.")
                                        .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id)
                                            {
                                            }
                                        }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                        setTextColor(getResources().getColor(R.color.colorPrimary));
                            }


                        }
                        catch (JSONException e)
                        {
                            progressDialog.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Cuenta.this.getActivity(),R.style.AlertDialogTheme));
                            builder
                                    .setTitle("ESTADO PROCESO")
                                    .setMessage(e.getMessage().toString())
                                    .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Cuenta.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Cuenta.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Cuenta.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Cuenta.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Cuenta.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Cuenta.this.getActivity(),R.style.AlertDialogTheme));
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
                headers.put("user",""+gestionSharedPreferences.getString("codEmpleado"));
                headers.put("tokenFCM", ""+FirebaseInstanceId.getInstance().getToken());
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void WebServiceRegistroCambiosCuenta(final String nombre, final String email)
    {
        progressDialog = new ProgressDialog(new android.support.v7.view.ContextThemeWrapper(Cuenta.this.getActivity(),R.style.AppCompatAlertDialogStyle));
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Guardando los cambios, espera un momento...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        String _urlWebService = vars.ipServer.concat("/ws/CambioDatosColaborador");

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

                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Cuenta.this.getActivity(),R.style.AlertDialogTheme));
                                builder
                                        .setTitle("AgradeSER")
                                        .setMessage(""+response.getString("message"))
                                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id)
                                            {
                                                /*gestionSharedPreferences.clear();
                                                Intent i=new Intent(Cuenta.this.getActivity(),Login.class);
                                                startActivity(i);
                                                getActivity().finish();*/
                                            }
                                        }).setCancelable(false).show();
                            }

                            else
                            {
                                progressDialog.dismiss();
                                Snackbar.make(getActivity().findViewById(android.R.id.content),
                                        ""+response.getString("message"), Snackbar.LENGTH_LONG).show();
                            }
                        }
                        catch (JSONException e)
                        {

                            progressDialog.dismiss();

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Cuenta.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Cuenta.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Cuenta.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Cuenta.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Cuenta.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Cuenta.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Cuenta.this.getActivity(),R.style.AlertDialogTheme));
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
                headers.put("codEmpleado", gestionSharedPreferences.getString("codEmpleado"));
                headers.put("nomEmpleado", nombre);
                headers.put("emailEmpleado", email);
                headers.put("tokenFCM", ""+FirebaseInstanceId.getInstance().getToken());
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
