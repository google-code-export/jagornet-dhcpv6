//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-520 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.04.06 at 11:28:50 PM EDT 
//


package com.agr.dhcpv6.server.config.xml;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for opaqueData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="opaqueData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="asciiValue" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="hexValue" type="{http://www.w3.org/2001/XMLSchema}hexBinary"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "opaqueData", propOrder = {
    "asciiValue",
    "hexValue"
})
@XmlSeeAlso({
    ServerIdOption.class,
    InterfaceIdOption.class,
    ClientIdOption.class
})
public class OpaqueData
    implements Serializable
{

    protected String asciiValue;
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    @XmlSchemaType(name = "hexBinary")
    protected byte[] hexValue;

    /**
     * Gets the value of the asciiValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAsciiValue() {
        return asciiValue;
    }

    /**
     * Sets the value of the asciiValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAsciiValue(String value) {
        this.asciiValue = value;
    }

    /**
     * Gets the value of the hexValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public byte[] getHexValue() {
        return hexValue;
    }

    /**
     * Sets the value of the hexValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHexValue(byte[] value) {
        this.hexValue = ((byte[]) value);
    }

}
