package fr.insee.sugoi.converter.ouganext;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Groupes", namespace = Namespace.ANNUAIRE)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListeGroupeType", propOrder = { "liste" })
public class Groupes {

    @XmlElement(name = "Groupe")
    protected List<Groupe> liste = new ArrayList<Groupe>();

    public List<Groupe> getListe() {
        return this.liste;
    }

    public void setListe(List<Groupe> liste) {
        this.liste = liste;
    }

}
