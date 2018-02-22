package com.ingeniapps.agradeser.beans;

import java.util.EmptyStackException;

/**
 * Created by Ingenia Applications on 29/09/2017.
 */

public class Empleado
{
    String codEmpleado;
    String codJefeEmpleado;
    String nomEmpleado;

    public String getNomJefeEmpleado() {
        return nomJefeEmpleadoEmpleado;
    }

    public void setNomJefeEmpleado(String nomJefeEmpleadoEmpleado) {
        this.nomJefeEmpleadoEmpleado = nomJefeEmpleadoEmpleado;
    }

    String nomJefeEmpleadoEmpleado;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    String type;


    public String getCodEmpleado() {
        return codEmpleado;
    }

    public void setCodEmpleado(String codEmpleado) {
        this.codEmpleado = codEmpleado;
    }

    public String getCodJefeEmpleado() {
        return codJefeEmpleado;
    }

    public void setCodJefeEmpleado(String codJefeEmpleado) {
        this.codJefeEmpleado = codJefeEmpleado;
    }

    public String getNomEmpleado() {
        return nomEmpleado;
    }

    public void setNomEmpleado(String nomEmpleado) {
        this.nomEmpleado = nomEmpleado;
    }

    public Empleado()
    {

    }

}
