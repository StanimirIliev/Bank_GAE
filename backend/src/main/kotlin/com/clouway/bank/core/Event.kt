package com.clouway.bank.core

/**
 * @author Stanimir Iliev <stanimir.iliev@clouway.com>
 */
 
interface Event {
    fun getAttributes() : Map<String, String>
}