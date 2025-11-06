package in.ashar.my_edms.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Metadata {
    @Id
    private String docId;

    private String title;

    @Column(length = 1000)
    private String description;

    private String tags;

    public void setDescription(String description) {
        if (description != null && description.length() > 1000) {
            this.description = description.substring(0, 1000);
        } else {
            this.description = description;
        }
    }
}
