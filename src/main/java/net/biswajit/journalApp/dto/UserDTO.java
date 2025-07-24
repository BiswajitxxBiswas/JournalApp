package net.biswajit.journalApp.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {


    @NotEmpty
    @Schema(description = "The User's username")
    private String userName;

    @NotEmpty
    private String password;

    private LocalDate date;
    private String email;
    private boolean sentimentAnalysis;
}
