package com.ingeniapps.agradeser.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.ingeniapps.agradeser.fragment.Cuenta;
import com.ingeniapps.agradeser.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.agradeser.vars.vars;
import com.ingeniapps.agradeser.volley.ControllerSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;

public class CambioClave extends AppCompatActivity
{
    EditText clave,confirmarClave;
    Button buttonGuardarClave;
    private ProgressDialog progressDialog;
    com.ingeniapps.agradeser.vars.vars vars;
    com.ingeniapps.agradeser.sharedPreferences.gestionSharedPreferences gestionSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambio_clave);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        gestionSharedPreferences=new gestionSharedPreferences(this);
        vars=new vars();

        clave=(EditText) findViewById(R.id.clave);
        confirmarClave=(EditText) findViewById(R.id.confirmarClave);
        buttonGuardarClave=(Button)findViewById(R.id.buttonGuardarClave);
        buttonGuardarClave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (clave.getText().toString().trim().length()<8)
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "La clave debe tener al menos 8 caracteres, por favor...", Snackbar.LENGTH_LONG).show();
                    view.requestFocus();
                    return;
                }

                if(!clave.getText().toString().equals(confirmarClave.getText().toString()))
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Las contraseñas no coinciden, verifica por favor...", Snackbar.LENGTH_LONG).show();
                    view.requestFocus();
                    return;
                }

                WebServiceRegistroCambiosClave(""+clave.getText().toString());
            }
        });
    }

    private void WebServiceRegistroCambiosClave(final String clave)
    {
        progressDialog = new ProgressDialog(new android.support.v7.view.ContextThemeWrapper(CambioClave.this,R.style.AppCompatAlertDialogStyle));
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Guardando su contraseña, espera un momento...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        String _urlWebService = vars.ipServer.concat("/ws/CambioClave");

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

                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(CambioClave.this,R.style.AlertDialogTheme));
                                builder
                                        .setTitle("AgradeSER")
                                        .setMessage(""+response.getString("message"))
                                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id)
                                            {
                                                gestionSharedPreferences.clear();
                                                Intent i=new Intent(CambioClave.this,Login.class);
                                                startActivity(i);
                                                finish();
                                            }
                                        }).setCancelable(false).show();
                            }

                            else
                            {
                                progressDialog.dismiss();
                                Snackbar.make(findViewById(android.R.id.content),
                                        ""+response.getString("message"), Snackbar.LENGTH_LONG).show();
                            }
                        }
                        catch (JSONException e)
                        {

                            progressDialog.dismiss();

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(CambioClave.this,R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(CambioClave.this,R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(CambioClave.this,R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(CambioClave.this,R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(CambioClave.this,R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(CambioClave.this,R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(CambioClave.this,R.style.AlertDialogTheme));
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
                headers.put("clvEmpleado", clave);
                headers.put("tokenFCM", ""+ FirebaseInstanceId.getInstance().getToken());
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }
}
