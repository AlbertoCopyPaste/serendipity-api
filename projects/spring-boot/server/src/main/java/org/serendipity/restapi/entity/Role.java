package org.serendipity.restapi.entity;

import lombok.*;
import org.serendipity.restapi.type.PartyType;

import javax.persistence.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Role {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  @Column(name = "id", nullable = false)
  private Long id;

  @Builder.Default
  private String role = "Member";

  @Column(nullable = false)
  private Long partyId;

  @Column(nullable = false)
  private PartyType partyType;

  @Column(nullable = false)
  private String partyName;

  @Column(nullable = false)
  private String partyEmail;

  @Column(nullable = false)
  private String partyPhoneNumber;

  @Builder.Default
  private String relationship = "Membership";

  @Builder.Default
  private String reciprocalRole = "Organisation";

  @Column(nullable = false)
  private Long reciprocalPartyId;

  @Column(nullable = false)
  private PartyType reciprocalPartyType;

  @Column(nullable = false)
  private String reciprocalPartyName;

  @Column(nullable = false)
  private String reciprocalPartyEmail;

  @Column(nullable = false)
  private String reciprocalPartyPhoneNumber;

  @Override
  public boolean equals(Object o) {

    if (this == o)
      return true;

    if (!(o instanceof Role))
      return false;

    Role other = (Role) o;

    return id != 0L && id.equals(other.getId());
  }

  @Override
  public int hashCode() {
    return 31;
  }

}

// https://jsonapi.org/format/#document-resource-object-relationships
