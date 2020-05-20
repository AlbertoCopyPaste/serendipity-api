package org.serendipity.restapi.model;

import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.util.Date;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class IndividualNameModel  extends RepresentationModel<IndividualNameModel> {

  private Long id;
  private String type;
  private String title;
  private String givenName;
  private String middleName;
  private String familyName;
  private String honorific;
  private String salutation;
  private String preferredName;
  private String initials;
  private Date fromDate;
  private Date toDate;

}
