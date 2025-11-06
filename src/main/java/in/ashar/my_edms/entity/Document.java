package in.ashar.my_edms.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Document {
    @Id
    private String id;
    private String filename;
    private String contentType;
    private long size;
}
