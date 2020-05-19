package org.serendipity.restapi.database.seed.au;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.serendipity.restapi.entity.*;
import org.serendipity.restapi.repository.*;
import org.serendipity.restapi.type.PartyType;
import org.serendipity.restapi.type.au.IdentifierLifecycleStatus;
import org.serendipity.restapi.type.au.IdentifierType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.HashSet;

@Component
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
public class HouseOfRepresentatives implements CommandLineRunner {

  static final String PATH = "sample-data/SurnameRepsCSV.csv";

  static final int HONORIFIC = 0;
  static final int SALUTATION = 1;
  static final int POST_NOMINALS = 2;
  static final int SURNAME = 3;
  static final int FIRST_NAME = 4;
  static final int OTHER_NAME = 5;
  static final int PREFERRED_NAME = 6;
  static final int INITIALS = 7;
  static final int ELECTORATE = 8;
  static final int POLITICAL_PARTY = 10;
  static final int SEX = 11;
  // static final int TITLE = 0; // Parliamentary Title,Ministerial Title

  @Autowired
  private AddressRepository addressRepository;

  @Autowired
  private IdentifierRepository identifierRepository;

  @Autowired
  private IndividualRepository individualRepository;

  @Autowired
  private OrganisationRepository organisationRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Override
  @Transactional
  public void run(String... args) throws Exception {

    log.info("Loading members of the House of Representatives ...");

    try {

      //
      // Example Identifier
      //

      Timestamp currentTime = new Timestamp(System.currentTimeMillis());

      Identifier identifier = Identifier.builder()
        .type(IdentifierType.ABN.getCode())
        .value("85 087 326 690")
        .register(IdentifierType.ABN.getRegister())
        .lifecycleStatus(IdentifierLifecycleStatus.ACTIVE.toString())
        .fromDate(currentTime)
        .build();

      identifierRepository.save(identifier);

      try {

        ObjectMapper mapper = new ObjectMapper();

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        log.info("identifier:  {}", "\n" + mapper.writeValueAsString(identifier));

      } catch (JsonProcessingException jpe) {

        log.error("House of Representatives - JSON Processing Exception");
      }

      //
      // Parliament House Address
      //

      Pageable pageable = PageRequest.of(0, 1);

      Page<Address> addresses = addressRepository.findByName("The Senate", pageable);
      Address parliamentHouse = addresses.getContent().get(0);

      //
      // Process sample data file
      //

      InputStream resource = new ClassPathResource(PATH).getInputStream();

      BufferedReader buffer = new BufferedReader(new InputStreamReader(resource));

      String line = buffer.readLine();

      log.info("Header: {}", line);

      while ((line = buffer.readLine()) != null && !line.isEmpty()) {

        // Note: No support for strings with embedded comma's, for example: "Commonwealth Parliament Offices, Suite 8"
        String[] fields = line.split(",");

        String displayName = fields[SURNAME] + ", " + fields[HONORIFIC] + " " + fields[FIRST_NAME];

        Party individualParty = Party.builder()
          .type(PartyType.INDIVIDUAL)
          .displayName(displayName)
          .addresses(new HashSet<Address>())
          .roles(new HashSet<Role>())
          .build();

        String email = fields[FIRST_NAME].toLowerCase() + "." + fields[SURNAME].toLowerCase() + "@aph.gov.au";

        Individual individual = Individual.builder()
          .party(individualParty)
          .title(fields[HONORIFIC])
          .givenName(fields[FIRST_NAME])
          .middleName(fields[OTHER_NAME])
          .familyName(fields[SURNAME])
          .honorific(fields[POST_NOMINALS])
          .salutation(fields[SALUTATION])
          .preferredName(fields[PREFERRED_NAME])
          .initials(fields[INITIALS])
          .sex(fields[SEX])
          .email(email)
          .phoneNumber("")
          .photoUrl("")
          .electorate(fields[ELECTORATE])
          .build();

        individualRepository.save(individual);

        Role role = Role.builder()
          .role("Member")
          .partyId(individual.getParty().getId())
          .partyType(individual.getParty().getType())
          .partyName(individual.getParty().getDisplayName())
          .partyEmail(individual.getEmail())
          .partyPhoneNumber(individual.getPhoneNumber())
          .relationship("Membership")
          .reciprocalRole("Organisation")
          // .reciprocalPartyId(1L)
          // .reciprocalPartyType(PartyType.ORGANISATION)
          // .reciprocalPartyName("")
          // .reciprocalPartyEmail("")
          // .reciprocalPartyPhoneNumber("")
          .build();

        boolean membership = true;

        // "AG" | "ALP" | "CA" | "JLN" | "LP" | "NATS" | "PHON" | "IND
        String abbreviation = fields[POLITICAL_PARTY].toUpperCase();

        PoliticalParty politicalParty = PoliticalParty.valueOfAbbreviation(abbreviation);

        switch (politicalParty) {

          case AUSTRALIAN_GREENS:
          case AUSTRALIAN_LABOR_PARTY:
          case CENTRE_ALLIANCE:
          case JACQUI_LAMBIE_NETWORK:
          case LIBERAL_PARTY_OF_AUSTRALIA:
          case NATIONAL_PARTY_OF_AUSTRALIA:
          case PAULINE_HANSONS_ONE_NATION:

            // log.info("Political Party: {}", politicalParty.toString());

            Page<Organisation> organisations = organisationRepository.findByName(politicalParty.toString(), pageable);

            Organisation organisation = organisations.getContent().get(0);

            role.setReciprocalPartyId(organisation.getParty().getId());
            role.setReciprocalPartyType(organisation.getParty().getType());
            role.setReciprocalPartyName(organisation.getParty().getDisplayName());
            role.setReciprocalPartyEmail(organisation.getEmail());
            role.setReciprocalPartyPhoneNumber(organisation.getPhoneNumber());

            break;

          case INDEPENDENT:
          default:

            log.info("Political Party: {}", abbreviation);

            membership = false;

            break;
        }

        individualParty.getAddresses().add(parliamentHouse);

        if (membership) {
          roleRepository.save(role);
          individualParty.getRoles().add(role);
        }

        individualRepository.save(individual);

      }

      buffer.close();

      log.info("Loading members of the House of Representatives complete");

    } catch (IOException | NullPointerException e) {

      log.error("{}", e.getLocalizedMessage());
    }

  }

}

/*

        try {

          ObjectMapper mapper = new ObjectMapper();

          mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
          mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

          mapper.enable(SerializationFeature.INDENT_OUTPUT);

          log.info("{}", "\n" + mapper.writeValueAsString(individual));

        } catch (JsonProcessingException jpe) {

          log.error("House of Representatives - JSON Processing Exception");
        }

            try {

              ObjectMapper mapper = new ObjectMapper();

              mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
              mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

              mapper.enable(SerializationFeature.INDENT_OUTPUT);

              log.info("{}", "\n" + mapper.writeValueAsString(organisation));

            } catch (JsonProcessingException jpe) {

              log.error("House of Representatives - JSON Processing Exception");
            }

*/
