package fr.insee.ctsc.ldap.rest.it;

import org.junit.jupiter.api.Test;

import fr.insee.ctsc.ldap.batch.core.BatchType;
import fr.insee.ctsc.ldap.batch.core.TraitementBatch;
import fr.insee.ctsc.ldap.batch.exception.BatchException;
import fr.insee.ctsc.ldap.dao.exception.DaoException;
import fr.insee.ctsc.ldap.utils.DaoUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestBatchIt extends InitTests {

  public void testBatchSynchronisationGeneric(String argTypeDuBatch, List<String> resAttendu, String sqlRequest) {
    try {
      System.setProperty("fr.insee.database.wscontact.driverClassName", "org.h2.Driver");
      System.setProperty("fr.insee.database.wscontact.url",
          "jdbc:h2:mem:test;init=runscript from 'classpath:/db-data/init.sql'");
      System.setProperty("fr.insee.ctsc.ldap.profils.port", "10389");

      BatchType typeDuBatch = BatchType.valueOf(argTypeDuBatch);

      TraitementBatch traitementBatch = typeDuBatch.getClasseDeTraitement().getConstructor().newInstance();

      String[] argsBatch = { "NODOMAIN" };

      try {
        traitementBatch.execute(argsBatch);
        try {
          Connection connexion = DaoUtils.getConnection();

          PreparedStatement preparedStatement = DaoUtils.initialiserRequetePreparee(connexion,
              sqlRequest);
          ResultSet resultSet = preparedStatement.executeQuery();
          List<String> resTrouve = new ArrayList<String>();
          while (resultSet.next()) {
            resTrouve.add(resultSet.getString(1));
          }
          Collections.sort(resTrouve);

          connexion.close();

          assertTrue(resAttendu.equals(resTrouve));
        } catch (DaoException | SQLException e) {
          fail(e);
        }
      } catch (BatchException e) {
        fail(e);
      }
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  public void testBatchSynchronizationContact() {
    List<String> resAttendu = new ArrayList<>(Arrays.asList("sloopy1", "sloopy2"));
    String sqlRequest = "select * from contacts where domaineGestion in ('testbatch', 'domaineintru', 'nouniqueid')";
    testBatchSynchronisationGeneric(BatchType.MISE_A_JOUR_REFERENTIEL_CONTACT.name(), resAttendu, sqlRequest);
  }

  @Test
  public void testBatchSynchronizationOrganisation() {
    String argTypeDuBatch = BatchType.MISE_A_JOUR_REFERENTIEL_ORGANISATION.name();
    List<String> orgaAttendu = new ArrayList<>(Arrays.asList("orga1", "orga2"));
    String sqlRequest = "select * from organisations where domaineGestion in ('testbatch', 'domaineintru', 'nouniqueid')";
    testBatchSynchronisationGeneric(argTypeDuBatch, orgaAttendu, sqlRequest);
  }
}