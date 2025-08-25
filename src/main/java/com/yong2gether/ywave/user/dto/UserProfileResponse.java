package com.yong2gether.ywave.user.dto;

public class UserProfileResponse {
    private Long id;
    private String nickname;
    private String email;
    private String photoUrl;


    public UserProfileResponse(Long id, String nickname, String email, String photoUrl) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.photoUrl = photoUrl;
    }

    public Long getId() { return id; }
    public String getNickname() { return nickname; }
    public String getEmail() { return email; }
    public String getPhotoUrl() { return photoUrl; }
}
