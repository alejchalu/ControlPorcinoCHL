package com.control.porcinochl

import android.os.Parcel
import android.os.Parcelable
import java.util.*

data class Evento(
    val fecha: Date,
    val tipo: String,
    val idCerda: String,
    val color: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        Date(parcel.readLong()),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(fecha.time)
        parcel.writeString(tipo)
        parcel.writeString(idCerda)
        parcel.writeInt(color)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Evento> {
        override fun createFromParcel(parcel: Parcel): Evento = Evento(parcel)
        override fun newArray(size: Int): Array<Evento?> = arrayOfNulls(size)
    }
}