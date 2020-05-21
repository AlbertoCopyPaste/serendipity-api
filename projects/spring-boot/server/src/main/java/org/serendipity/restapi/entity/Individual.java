package org.serendipity.restapi.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(indexes = { @Index(name = "SORT_INDEX", columnList = "sort", unique = false) })
public class Individual {

  // An Individual is a person.
  // The Individual concept represents people about which an Enterprise wishes to maintain information.

  @Id
  private Long id;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "partyId")
  @MapsId
  private Party party;

  // @Embedded
  // private Name name;

  // @OrderColumn
  @Column(name = "sort", nullable = false)
  private String sort; // IndividualName.familyName

  // You cannot limit the size of a @OneToMany collection
  // See: https://vladmihalcea.com/the-best-way-to-map-a-onetomany-association-with-jpa-and-hibernate/

  @OneToMany(mappedBy = "individual", fetch = FetchType.EAGER)
  private Set<IndividualName> names;

  private String sex;

  private String gender;

  private String email;

  private String phoneNumber;

  private String photoUrl;

  private String electorate;

  @Temporal(TemporalType.DATE)
  private Date dateOfBirth;
  private String placeOfBirth;
  private String countryOfBirth;

  @Temporal(TemporalType.DATE)
  private Date dateOfDeath;
  private String placeOfDeath;
  private String countryOfDeath;

  // citizenship
  // residences

  private String relationshipLifecycleStatus;

  // occupation
  // positions
  
  @Override
  public boolean equals(Object o) {

    if (this == o)
      return true;

    if (!(o instanceof Individual))
      return false;

    Individual other = (Individual) o;

    // return id != 0L && id == other.getId();
    return id != 0L && id.equals(other.getId());
  }

  @Override
  public int hashCode() {
    return 31;
  }

}
