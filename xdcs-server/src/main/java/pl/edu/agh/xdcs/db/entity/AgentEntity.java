package pl.edu.agh.xdcs.db.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.edu.agh.xdcs.db.conf.InetAddressConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.Table;
import java.net.InetAddress;

/**
 * @author Kamil Jarosz
 */
@Getter
@Setter
@NoArgsConstructor
@Entity(name = "Agent")
@Table(name = "XDCS_AGENT_", indexes = {
        @Index(columnList = "NAME_", unique = true)
})
public class AgentEntity extends BaseEntity {
    @Column(name = "NAME_")
    private String name;

    @Column(name = "DISPLAY_NAME_")
    private String displayName;

    @Column(name = "STATUS_")
    @Enumerated(value = EnumType.STRING)
    private Status status;

    @Column(name = "IP_ADDRESS_")
    @Convert(converter = InetAddressConverter.class)
    private InetAddress address;

    public enum Status {
        UNAVAILABLE,
        READY,
        BUSY,
        OFFLINE,
    }
}
