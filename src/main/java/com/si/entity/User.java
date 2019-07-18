package com.si.entity;


import java.util.Date;


/**
 *
 * @author wstevens
 */
public class User
{
	private static final String UNIDENTIFIED = "UNIDENTIFIED";
	private static final String GUEST = "guest";
    private String id;
	private String username;
	private String firstName;
	private String lastName;
    private String fullName;
	private StoredImageInfo imageInfo;
    private String email;
	private Date birthdate;
	private String gender;
	private String password;
	private String passwordQuestion;
	private String passwordAnswer;
	private String timezone;
	private String cookieValue;
	private String registrationSource;
	private String verificationCode;
	private String verificationCodeIssuedTimestamp;
	private String verificationCodeCompletedTimestamp;
	private boolean inAgreement;
	private boolean isGuest;
	private boolean isUnidentified;
	private boolean isEnabled;
	private boolean isAdmin;
	private boolean isDeleted;
    private String insertTimestamp;
    private Date updateTimestamp;
    private Date deleteTimestamp;

	public static User newUnidentifiedUser() {
		User unidentified = new User();
		unidentified.id = null; // to be assigned by unique client ID
		unidentified.isUnidentified = true;
		return unidentified;
	}

	public static User newGuestUser(String userGuestImageUrl) {
		User guest = new User();
		guest.id = null; // to be assigned by unique client ID
		guest.isGuest = true;
		guest.username = GUEST;
		guest.fullName = "Guest";
		guest.firstName = "Guest";
		StoredImageInfo storedImageInfo = new StoredImageInfo();
		storedImageInfo.setSecureUrl(userGuestImageUrl);
		guest.imageInfo = storedImageInfo;
		return guest;
	}

	public boolean isUnidentified() {
		return isUnidentified;
	}

	public boolean isGuest() {
		return isGuest;
	}

	public StoredImageInfo getImageInfo() {
		return imageInfo;
	}

	public void setImageInfo(StoredImageInfo imageInfo) {
		this.imageInfo = imageInfo;
	}

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Date getBirthdate() {
		return birthdate;
	}
	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getPasswordQuestion() {
		return passwordQuestion;
	}
	public void setPasswordQuestion(String passwordQuestion) {
		this.passwordQuestion = passwordQuestion;
	}
	public String getPasswordAnswer() {
		return passwordAnswer;
	}
	public void setPasswordAnswer(String passwordAnswer) {
		this.passwordAnswer = passwordAnswer;
	}

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCookieValue() {
		return cookieValue;
	}
	public void setCookieValue(String cookieValue) {
		this.cookieValue = cookieValue;
	}
	public String getVerificationCode() {
		return verificationCode;
	}
	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}

	public String getVerificationCodeIssuedTimestamp() {
		return verificationCodeIssuedTimestamp;
	}

	public void setVerificationCodeIssuedTimestamp(String verificationCodeIssuedTimestamp) {
		this.verificationCodeIssuedTimestamp = verificationCodeIssuedTimestamp;
	}

	public String getVerificationCodeCompletedTimestamp() {
		return verificationCodeCompletedTimestamp;
	}

	public void setVerificationCodeCompletedTimestamp(String verificationCodeCompletedTimestamp) {
		this.verificationCodeCompletedTimestamp = verificationCodeCompletedTimestamp;
	}

	public boolean isEnabled() {
		return isEnabled;
	}
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	public boolean isAdmin() {
		return isAdmin;
	}
	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}
	public boolean isDeleted() {
		return isDeleted;
	}
	public void setDeleted(boolean deleted) {
		this.isDeleted = deleted;
	}
	public boolean isInAgreement() {
		return inAgreement;
	}
	public void setInAgreement(boolean inAgreement) {
		this.inAgreement = inAgreement;
	}

	public String getInsertTimestamp() {
		return insertTimestamp;
	}

	public void setInsertTimestamp(String insertTimestamp) {
		this.insertTimestamp = insertTimestamp;
	}

	public Date getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(Date updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public Date getDeleteTimestamp() {
        return deleteTimestamp;
    }

    public void setDeleteTimestamp(Date deleteTimestamp) {
        this.deleteTimestamp = deleteTimestamp;
    }

    public String getId () {
        return id;
    }

    public void setId (String id) {
        this.id = id;
    }

	public String getRegistrationSource() {
		return registrationSource;
	}

	public void setRegistrationSource(String registrationSource) {
		this.registrationSource = registrationSource;
	}

	public void setGuest(boolean guest) {
		isGuest = guest;
	}

	@Override
	public String toString() {
		return String.format(
				"User [username=%s, firstName=%s, lastName=%s, email=%s, registrationSource=%s]",
				username, firstName, lastName, email, registrationSource);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}
	
	
}
