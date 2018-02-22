package com.ingeniapps.agradeser.beans;

/**
 * Created by Ingenia Applications on 2/10/2017.
 */

public class Voto
{
    public String getNomEmpleado() {
        return nomEmpleado;
    }

    public void setNomEmpleado(String nomEmpleado) {
        this.nomEmpleado = nomEmpleado;
    }

    public String getFecVoto() {
        return fecVoto;
    }

    public void setFecVoto(String fecVoto) {
        this.fecVoto = fecVoto;
    }

    public String getDesMotivo() {
        return desMotivo;
    }

    public void setDesMotivo(String desMotivo) {
        this.desMotivo = desMotivo;
    }

    private String nomEmpleado;
    private String fecVoto;
    private String desMotivo;

    public String getPaisVoto() {
        return paisVoto;
    }

    public void setPaisVoto(String paisVoto) {
        this.paisVoto = paisVoto;
    }

    private String paisVoto;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String type;

    public Voto()
    {

    }
}
