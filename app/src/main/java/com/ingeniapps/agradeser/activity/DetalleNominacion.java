package com.ingeniapps.agradeser.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.vision.text.Text;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ingeniapps.agradeser.R;
import com.ingeniapps.agradeser.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.agradeser.volley.ControllerSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import com.ingeniapps.agradeser.vars.vars;


public class DetalleNominacion extends AppCompatActivity
{
    private TextView nomEmpleado, paisEmpleado, descEmpleado;
    private String nombre, pais, descripcion;
    private Toolbar toolbar;
    com.ingeniapps.agradeser.sharedPreferences.gestionSharedPreferences gestionSharedPreferences;
    vars vars;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_nominacion);
        //change id to Your id
        gestionSharedPreferences=new gestionSharedPreferences(DetalleNominacion.this);

        vars=new vars();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //this line shows back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setElevation(0);
        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                finish();
            }
        });


        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if (extras == null)
            {
                nombre = null;
                pais = null;
                descripcion = null;
            }
            else
            {
                nombre = extras.getString("nomEmpleado");
                pais = extras.getString("paisEmpleado");
                descripcion = extras.getString("motivoEmpleado");
            }
        }

        nomEmpleado=(TextView) findViewById(R.id.empleadoNominadoDetalle);
        paisEmpleado=(TextView) findViewById(R.id.paisNominadoDetalle);
        descEmpleado=(TextView) findViewById(R.id.motivoNominadoDetalle);

        nomEmpleado.setText(""+nombre);
        paisEmpleado.setText(""+pais);
        descEmpleado.setText(""+descripcion);
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
}
