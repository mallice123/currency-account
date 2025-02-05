package com.portfolio.recruitment.currencyaccount.connectors.db.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class AccountEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NotNull(message = "First name must not be null")
        @Size(min = 1, max = 20, message = "First name must be between 1 and 20 characters")
        private String firstName;

        @NotNull(message = "Last name must not be null")
        @Size(min = 1, max = 20, message = "Last name must be between 1 and 20 characters")
        private String lastName;

        @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
        @JoinColumn(name = "account_id")
        private List<BalanceEntity> balances;

        @Override
        public final boolean equals(Object object) {
                if (this == object) return true;
                if (object == null) return false;
                Class<?> oEffectiveClass = object instanceof HibernateProxy ? ((HibernateProxy) object).getHibernateLazyInitializer().getPersistentClass() : object.getClass();
                Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
                if (thisEffectiveClass != oEffectiveClass) return false;
                AccountEntity that = (AccountEntity) object;
                return getId() != null && Objects.equals(getId(), that.getId());
        }

        @Override
        public final int hashCode() {
                return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
        }

        @Override
        public String toString() {
                return getClass().getSimpleName() + "(" +
                        "id = " + id + ")";
        }
}
