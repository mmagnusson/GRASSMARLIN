//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2025.07.11 at 12:53:47 PM CDT 
//


package core.fingerprint3;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ByteTestFunction complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ByteTestFunction">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="GT" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *           &lt;element name="LT" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *           &lt;element name="GTE" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *           &lt;element name="LTE" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *           &lt;element name="EQ" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *           &lt;element name="AND" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *           &lt;element name="OR" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;/choice>
 *         &lt;element ref="{}AndThen"/>
 *       &lt;/sequence>
 *       &lt;attribute ref="{}PostOffset default="0""/>
 *       &lt;attribute ref="{}Relative"/>
 *       &lt;attribute ref="{}Endian default="BIG""/>
 *       &lt;attribute ref="{}Offset default="0""/>
 *       &lt;attribute ref="{}Bytes default="1""/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ByteTestFunction", propOrder = {
    "gt",
    "lt",
    "gte",
    "lte",
    "eq",
    "and",
    "or",
    "andThen"
})
public class ByteTestFunction {

    @XmlElement(name = "GT")
    protected BigInteger gt;
    @XmlElement(name = "LT")
    protected BigInteger lt;
    @XmlElement(name = "GTE")
    protected BigInteger gte;
    @XmlElement(name = "LTE")
    protected BigInteger lte;
    @XmlElement(name = "EQ")
    protected BigInteger eq;
    @XmlElement(name = "AND")
    protected BigInteger and;
    @XmlElement(name = "OR")
    protected BigInteger or;
    @XmlElement(name = "AndThen", required = true)
    protected AndThen andThen;
    @XmlAttribute(name = "PostOffset")
    protected Integer postOffset;
    @XmlAttribute(name = "Relative")
    protected Boolean relative;
    @XmlAttribute(name = "Endian")
    protected String endian;
    @XmlAttribute(name = "Offset")
    protected Integer offset;
    @XmlAttribute(name = "Bytes")
    protected Integer bytes;

    /**
     * Gets the value of the gt property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getGT() {
        return gt;
    }

    /**
     * Sets the value of the gt property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setGT(BigInteger value) {
        this.gt = value;
    }

    /**
     * Gets the value of the lt property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getLT() {
        return lt;
    }

    /**
     * Sets the value of the lt property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setLT(BigInteger value) {
        this.lt = value;
    }

    /**
     * Gets the value of the gte property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getGTE() {
        return gte;
    }

    /**
     * Sets the value of the gte property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setGTE(BigInteger value) {
        this.gte = value;
    }

    /**
     * Gets the value of the lte property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getLTE() {
        return lte;
    }

    /**
     * Sets the value of the lte property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setLTE(BigInteger value) {
        this.lte = value;
    }

    /**
     * Gets the value of the eq property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getEQ() {
        return eq;
    }

    /**
     * Sets the value of the eq property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setEQ(BigInteger value) {
        this.eq = value;
    }

    /**
     * Gets the value of the and property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getAND() {
        return and;
    }

    /**
     * Sets the value of the and property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setAND(BigInteger value) {
        this.and = value;
    }

    /**
     * Gets the value of the or property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getOR() {
        return or;
    }

    /**
     * Sets the value of the or property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setOR(BigInteger value) {
        this.or = value;
    }

    /**
     * Gets the value of the andThen property.
     * 
     * @return
     *     possible object is
     *     {@link AndThen }
     *     
     */
    public AndThen getAndThen() {
        return andThen;
    }

    /**
     * Sets the value of the andThen property.
     * 
     * @param value
     *     allowed object is
     *     {@link AndThen }
     *     
     */
    public void setAndThen(AndThen value) {
        this.andThen = value;
    }

    /**
     * Gets the value of the postOffset property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getPostOffset() {
        if (postOffset == null) {
            return  0;
        } else {
            return postOffset;
        }
    }

    /**
     * Sets the value of the postOffset property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPostOffset(Integer value) {
        this.postOffset = value;
    }

    /**
     * Gets the value of the relative property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isRelative() {
        if (relative == null) {
            return false;
        } else {
            return relative;
        }
    }

    /**
     * Sets the value of the relative property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRelative(Boolean value) {
        this.relative = value;
    }

    /**
     * Gets the value of the endian property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEndian() {
        if (endian == null) {
            return "BIG";
        } else {
            return endian;
        }
    }

    /**
     * Sets the value of the endian property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEndian(String value) {
        this.endian = value;
    }

    /**
     * Gets the value of the offset property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getOffset() {
        if (offset == null) {
            return  0;
        } else {
            return offset;
        }
    }

    /**
     * Sets the value of the offset property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setOffset(Integer value) {
        this.offset = value;
    }

    /**
     * Gets the value of the bytes property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getBytes() {
        if (bytes == null) {
            return  1;
        } else {
            return bytes;
        }
    }

    /**
     * Sets the value of the bytes property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setBytes(Integer value) {
        this.bytes = value;
    }

}
