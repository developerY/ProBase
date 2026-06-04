package com.zoewave.probase.kocolor.features.chemicals.data.model

import com.google.gson.annotations.SerializedName

data class PubChemResponse(
    @SerializedName("PC_Compounds") val compounds: List<Compound>?
)

data class Compound(
    @SerializedName("id") val id: CompoundId?,
    @SerializedName("props") val props: List<CompoundProperty>?,
    @SerializedName("count") val count: CompoundCount?
)

data class CompoundId(
    @SerializedName("id") val id: IdValue?
)

data class IdValue(
    @SerializedName("cid") val cid: Int?
)

data class CompoundProperty(
    @SerializedName("urn") val urn: PropertyUrn?,
    @SerializedName("value") val value: PropertyValue?
)

data class PropertyUrn(
    @SerializedName("label") val label: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("release") val release: String?
)

data class PropertyValue(
    @SerializedName("sval") val sval: String?,
    @SerializedName("fval") val fval: Double?,
    @SerializedName("ival") val ival: Int?
)

data class CompoundCount(
    @SerializedName("heavy_atom") val heavyAtom: Int?,
    @SerializedName("atom_chiral") val atomChiral: Int?,
    @SerializedName("atom_chiral_def") val atomChiralDef: Int?,
    @SerializedName("atom_chiral_undef") val atomChiralUndef: Int?,
    @SerializedName("bond_chiral") val bondChiral: Int?,
    @SerializedName("bond_chiral_def") val bondChiralDef: Int?,
    @SerializedName("bond_chiral_undef") val bondChiralUndef: Int?,
    @SerializedName("isotope_atom") val isotopeAtom: Int?,
    @SerializedName("covalent_unit") val covalentUnit: Int?,
    @SerializedName("isomeric_atom") val isomericAtom: Int?
)
