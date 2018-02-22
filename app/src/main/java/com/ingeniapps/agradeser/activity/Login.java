package com.ingeniapps.agradeser.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowManager;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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

public class Login extends AppCompatActivity
{
    EditText numIdentificacion;
    EditText contraseña;
    public vars vars;
    private String numIdentificacionUsuario,contraseñaUsuario,MyToken,tokenFCM;
    private Button botonLogin;
    gestionSharedPreferences gestionSharedPreferences;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private ProgressDialog progressDialog;
    private String indCambioClv;
    private String nomColaborador;


    private Boolean guardarSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        vars=new vars();
        gestionSharedPreferences=new gestionSharedPreferences(this);

        //COMPROBAMOS LA SESION DEL USUARIO
        guardarSesion=gestionSharedPreferences.getBoolean("GuardarSesion");
        if (guardarSesion==true)
        {
            cargarActivityPrincipal();
        }

        numIdentificacion=(EditText)findViewById(R.id.numeroIdentidad);
        contraseña=(EditText)findViewById(R.id.contraseña);

        botonLogin=(Button)findViewById(R.id.buttonIngresar);
        botonLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                numIdentificacionUsuario=numIdentificacion.getText().toString();
                contraseñaUsuario=contraseña.getText().toString();

                if (TextUtils.isEmpty(numIdentificacionUsuario))
                {
                    numIdentificacion.setError("Digite su número de identificación");
                    numIdentificacion.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(contraseñaUsuario))
                {
                    contraseña.setError("Digite su contraseña");
                    contraseña.requestFocus();
                    return;
                }

                WebServiceLogin(numIdentificacionUsuario,contraseñaUsuario);

            }
        });

        if(checkPlayServices())
        {
            if(!TextUtils.isEmpty(FirebaseInstanceId.getInstance().getToken()))
            {
                tokenFCM=FirebaseInstanceId.getInstance().getToken();
            }
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Login.this,R.style.AlertDialogTheme));
            builder
                    .setTitle("GOOGLE PLAY SERVICES")
                    .setMessage("Se ha encontrado un error con los servicios de Google Play, actualizalo y vuelve a ingresar.")
                    .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            finish();
                        }
                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                    setTextColor(getResources().getColor(R.color.colorPrimary));
        }
    }

    public void cargarActivityPrincipal()
    {
        Intent intent = new Intent(Login.this, Principal.class);
        intent.putExtra("indCambioClv",gestionSharedPreferences.getString("indCambioClv"));
        intent.putExtra("nomColaborador",gestionSharedPreferences.getString("nomColaborador"));
        startActivity(intent);
        Login.this.finish();
    }

    private boolean checkPlayServices()
    {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS)
        {
            if(googleAPI.isUserResolvableError(result))
            {
                googleAPI.getErrorDialog(this, result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }

            return false;
        }

        return true;
    }

    private void WebServiceLogin(final String numIdentificacionUsuario, final String contraseñaUsuario)
    {
        String _urlWebService=vars.ipServer.concat("/ws/Login");

        progressDialog = new ProgressDialog(new ContextThemeWrapper(Login.this,R.style.AppCompatAlertDialogStyle));
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Validando colaborador, espera un momento...");
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
                            String message=response.getString("message");

                            if(status)
                            {
                                //OBTENEMOS DATOS DEL USUARIO PARA GUARDAR SU SESION
                                gestionSharedPreferences.putBoolean("GuardarSesion", true);
                                gestionSharedPreferences.putString("MyToken",""+response.getString("MyToken"));
                                gestionSharedPreferences.putString("codEmpleado",""+response.getString("codEmpleado"));
                                gestionSharedPreferences.putString("nomColaborador",""+response.getString("nomColaborador"));
                                gestionSharedPreferences.putString("indCambioClv",""+response.getString("indCambioClv"));
                                indCambioClv=""+response.getString("indCambioClv");
                                nomColaborador=""+response.getString("nomColaborador");
                                Intent intent=new Intent(Login.this, Principal.class);
                                intent.putExtra("indCambioClv",response.getString("indCambioClv"));
                                intent.putExtra("nomColaborador",response.getString("nomColaborador"));
                                startActivity(intent);
                                finish();
                            }

                            else
                            //if(sesionAbierta)
                            {
                                progressDialog.dismiss();
                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Login.this,R.style.AlertDialogTheme));
                                builder
                                        .setTitle("AgradeSER")
                                        .setMessage(message)
                                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id)
                                            {

                                            }
                                        }).setCancelable(true).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                        setTextColor(getResources().getColor(R.color.colorPrimary));
                            }
                        }
                        catch (JSONException e)
                        {
                            progressDialog.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Login.this,R.style.AlertDialogTheme));
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
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        progressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Login.this,R.style.AlertDialogTheme));
                        builder
                                .setTitle("ESTADO PROCESO")
                                .setMessage(error.toString())
                                .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                    }
                                }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                setTextColor(getResources().getColor(R.color.colorPrimary));

                        if (error instanceof TimeoutError)
                        {
                            progressDialog.dismiss();
                            builder
                                    .setTitle("ESTADO PROCESO")
                                    .setMessage(error.toString())
                                    .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else
                        if (error instanceof NoConnectionError)
                        {
                            progressDialog.dismiss();
                            builder
                                    .setTitle("ESTADO PROCESO")
                                    .setMessage(error.toString())
                                    .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
                        }

                        else

                        if (error instanceof AuthFailureError)
                        {
                            progressDialog.dismiss();
                            builder
                                    .setTitle("ESTADO PROCESO")
                                    .setMessage(error.toString())
                                    .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
                        }

                        else

                        if (error instanceof ServerError)
                        {
                            progressDialog.dismiss();
                            builder
                                    .setTitle("ESTADO REGISTRO")
                                    .setMessage(error.toString())
                                    .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else
                        if (error instanceof NetworkError)
                        {
                            progressDialog.dismiss();
                            builder
                                    .setTitle("ESTADO PROCESO")
                                    .setMessage(error.toString())
                                    .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else
                        if (error instanceof ParseError)
                        {
                            progressDialog.dismiss();
                            builder
                                    .setTitle("ESTADO PROCESO")
                                    .setMessage(error.toString())
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
                })
        {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap <String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                headers.put("user", numIdentificacionUsuario);
                headers.put("pass", contraseñaUsuario);
                headers.put("codSistema", "1");
                headers.put("tokenFCM", ""+FirebaseInstanceId.getInstance().getToken());
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    /*private void WebServiceCerrarSesion(final String emailUsuario)
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Cerrando sesiónes, espera un momento ...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        String _urlWebService = vars.ipServer.concat("/ws/CerrarSesion");

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
                                Snackbar.make(findViewById(android.R.id.content),
                                        "Las sesiones han sido cerradas con éxito. Ingresa de nuevo.", Snackbar.LENGTH_LONG).show();
                            }

                            else
                            {
                                progressDialog.dismiss();
                                Snackbar.make(findViewById(android.R.id.content),
                                        "Erros cerrando sesiones, contactanos por la opción Soporte.", Snackbar.LENGTH_LONG).show();
                            }
                        }
                        catch (JSONException e)
                        {
                            progressDialog.dismiss();


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Login.this,R.style.AlertDialogTheme));
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


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Login.this,R.style.AlertDialogTheme));
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


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Login.this,R.style.AlertDialogTheme));
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


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Login.this,R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Login.this,R.style.AlertDialogTheme));
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


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Login.this,R.style.AlertDialogTheme));
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


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Login.this,R.style.AlertDialogTheme));
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
                headers.put("emailUsuario", emailUsuario);
                //headers.put("MyToken", sharedPreferences.getString("MyToken"));
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }*/
}
