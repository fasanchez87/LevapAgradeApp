package com.ingeniapps.agradeser.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ingeniapps.agradeser.R;
import com.ingeniapps.agradeser.app.Config;
import com.ingeniapps.agradeser.fragment.Nominar;
import com.ingeniapps.agradeser.fragment.HistorialNominaciones;
import com.ingeniapps.agradeser.fragment.Cuenta;
import com.ingeniapps.agradeser.helper.BottomNavigationViewHelper;
import com.ingeniapps.agradeser.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.agradeser.util.NotificationUtils;
import com.ingeniapps.agradeser.volley.ControllerSingleton;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.HashMap;
import java.util.Map;

import com.ingeniapps.agradeser.vars.vars;


public class Principal extends AppCompatActivity
{
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    vars vars;
    private String tokenFCM;
    private String indCambioClv;
    private String nomColaborador;
    private String indicaPush;
    com.ingeniapps.agradeser.sharedPreferences.gestionSharedPreferences gestionSharedPreferences;
    public String currentVersion = null;
    private String html="";
    private String versionPlayStore="";
    Context context;
    Dialog dialog;
    private BroadcastReceiver mRegistrationBroadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        gestionSharedPreferences=new gestionSharedPreferences(Principal.this);



        tokenFCM="";
        vars=new vars();
        context = this;

        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if (extras == null)
            {
                indCambioClv = null;
                nomColaborador = null;
                indicaPush = null;
            }
            else
            {
                indCambioClv = extras.getString("indCambioClv");
                nomColaborador = extras.getString("nomColaborador");
                indicaPush = extras.getString("indicaPush");
            }
        }

        try
        {
            currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

        if(checkPlayServices())
        {
            if(!TextUtils.isEmpty(FirebaseInstanceId.getInstance().getToken()))
            {
                tokenFCM=FirebaseInstanceId.getInstance().getToken();
            }
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Principal.this,R.style.AlertDialogTheme));
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



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);

        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, new Nominar());
        fragmentTransaction.commit();

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener()
                {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item)
                    {
                        Fragment fragment = null;
                        Class fragmentClass;

                        switch (item.getItemId())
                        {
                            case R.id.action_nominar:
                                fragmentClass = Nominar.class;
                                 break;
                            case R.id.action_historial:
                                fragmentClass = HistorialNominaciones.class;
                                break;
                            case R.id.action_cuenta:
                                fragmentClass = Cuenta.class;
                                break;
                            default:
                                fragmentClass = Nominar.class;
                        }

                        try
                        {
                            fragment = (Fragment) fragmentClass.newInstance();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.frame_layout, fragment);
                        fragmentTransaction.commit();
                        return true;
                    }
                });

        if(TextUtils.equals(indCambioClv,"0"))//NO HA CAMDIADO SU CLAVE
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Principal.this,R.style.AlertDialogTheme));
            builder
                    .setTitle("AgradeSER")
                    .setMessage("Hola, "+nomColaborador+" Debes cambiar tu clave de acceso para poder usar la aplicación, Hazlo ahora!")
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            bottomNavigationView.setSelectedItemId(R.id.action_cuenta);
                            bottomNavigationView.getMenu().findItem(R.id.action_nominar).setEnabled(false);
                            bottomNavigationView.getMenu().findItem(R.id.action_historial).setEnabled(false);
                            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.frame_layout, new Cuenta());
                            fragmentTransaction.commit();
                        }
                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                    setTextColor(getResources().getColor(R.color.colorPrimary));
        }

        mRegistrationBroadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                // checking for type intent filter
                if (intent.getAction().equals(Config.PUSH_NOMINACION))
                {
                    if(!(Principal.this).isFinishing())
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Principal.this, R.style.AlertDialogTheme));
                        builder
                                .setTitle("AgradeSER")
                                .setMessage("Hola! Tienes una nueva nominación.")
                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        bottomNavigationView.setSelectedItemId(R.id.action_historial);
                                        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                                        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                        fragmentTransaction.replace(R.id.frame_layout, new HistorialNominaciones());
                                        fragmentTransaction.commit();
                                    }
                                }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                setTextColor(getResources().getColor(R.color.colorPrimary));
                    }
                }
            }
        };

        if(TextUtils.equals(indicaPush,"pushNominacion"))
        {
            bottomNavigationView.setSelectedItemId(R.id.action_historial);
            android.support.v4.app.FragmentManager fragmentManagerr = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransactionn = fragmentManagerr.beginTransaction();
            fragmentTransactionn.replace(R.id.frame_layout, new HistorialNominaciones());
            fragmentTransactionn.commit();
        }

    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_cierre_sesion, menu);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(new android.support.v7.view.ContextThemeWrapper(this, R.style.AlertDialogTheme));
            builder
                    .setTitle("Salir de la Aplicación")
                    .setMessage("¿Deseas salir de la aplicación justo ahora?")
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int id)
                {

                }
            }).show();
        }

        return false;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_cierre_sesion:
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Principal.this,R.style.AlertDialogTheme));
                builder
                        .setMessage("¿Esta seguro de cerrar sesión ahora?")
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {
                                gestionSharedPreferences.clear();
                                Intent i = new Intent(Principal.this, Login.class);
                                startActivity(i);
                                finish();
                            }
                        }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {

                            }
                        }).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        //COUNTER DE NOTIFICACIONES
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);

       /*     finish();
            ControllerSingleton.getInstance().getReqQueue().cancelAll("");*/

    }

    @Override
    public void onResume()
    {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOMINACION));
        NotificationUtils.clearNotifications(this);

        _webServicecheckVersionAppPlayStore();
        updateTokenFCMToServer();
    }

    public static int compareVersions(String version1, String version2)//COMPARAR VERSIONES
    {
        String[] levels1 = version1.split("\\.");
        String[] levels2 = version2.split("\\.");

        int length = Math.max(levels1.length, levels2.length);
        for (int i = 0; i < length; i++){
            Integer v1 = i < levels1.length ? Integer.parseInt(levels1[i]) : 0;
            Integer v2 = i < levels2.length ? Integer.parseInt(levels2[i]) : 0;
            int compare = v1.compareTo(v2);
            if (compare != 0){
                return compare;
            }
        }
        return 0;
    }


    private void _webServicecheckVersionAppPlayStore()
    {
        String _urlWebService = "https://play.google.com/store/apps/details?id=com.ingeniapps.agradeser";

        StringRequest jsonObjReq = new StringRequest (Request.Method.GET, _urlWebService,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        html=response;
                        Document document= Jsoup.parse(html);
                        versionPlayStore=document.select("div[itemprop=softwareVersion]").first().ownText();
                        Log.i("softwareVersion","softwareVersion: "+versionPlayStore);

                        if(compareVersions(currentVersion,versionPlayStore) == -1)
                        {
                            if(!((Activity) context).isFinishing())
                            {
                                dialog = new Dialog(Principal.this);
                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dialog.setCancelable(false);
                                dialog.setContentView(R.layout.custom_dialog);

                                Button dialogButton = (Button) dialog.findViewById(R.id.btn_dialog);
                                dialogButton.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setData(Uri.parse("market://details?id=com.ingeniapps.agradeser"));
                                        startActivity(intent);
                                    }
                                });

                                dialog.show();
                            }
                        }
                    }

                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap <String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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
