package com.xxh.cyclelink

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest



object SupabaseManager {

    val client = createSupabaseClient(
        supabaseUrl = "https://jcdetbkaymoyhtjotbxn.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpjZGV0YmtheW1veWh0am90YnhuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDIwMTAyMjMsImV4cCI6MjA1NzU4NjIyM30.dwDiZwvQWbVUsd1zuLWzPePNElUTVdjYx2lPikDtPAg"
    ) {
        install(Postgrest)
    }
}