package gov.ca.emsa.pulse.auth.user;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import gov.ca.emsa.pulse.auth.permission.GrantedPermission;
import gov.ca.emsa.pulse.common.domain.AlternateCareFacility;

public class CommonUser implements User {

    private static final long serialVersionUID = -4255948572251487878L;

    private Long id;
    private String subjectName;
    private String firstName;
    private String lastName;
    private String email;

    // SAML components
    private String user_id;
    private String username;
    private String auth_source;
    private String full_name;
    private String organization;
    private String purpose_for_use;

    private String role;

    private Set<GrantedPermission> permissions = new HashSet<GrantedPermission>();
    private boolean authenticated = true;
    private String jwt;
    private AlternateCareFacility acf;
    private Long liferayStateId;
    private Long liferayAcfId;

    public final static String ROLE_ADMIN = "ROLE_ADMIN";
    public final static String ROLE_ORG_ADMIN = "ROLE_ORG_ADMIN";
    public final static String ROLE_PROVIDER = "ROLE_PROVIDER";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
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

    public String getuser_id() {
        return user_id;
    }

    public void setuser_id(String user_id) {
        this.user_id = user_id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setusername(String username) {
        this.username = username;
    }

    public String getauth_source() {
        return auth_source;
    }

    public void setauth_source(String auth_source) {
        this.auth_source = auth_source;
    }

    public String getfull_name() {
        return full_name;
    }

    public void setfull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getorganization() {
        return organization;
    }

    public void setorganization(String organization) {
        this.organization = organization;
    }

    public String getpurpose_for_use() {
        return purpose_for_use;
    }

    public void setpurpose_for_use(String purpose_for_use) {
        this.purpose_for_use = purpose_for_use;
    }

    public String getrole() {
        return role;
    }

    public void setrole(String role) {
        this.role = role;
    }

    public Set<GrantedPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<GrantedPermission> permissions) {
        this.permissions = permissions;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public AlternateCareFacility getAcf() {
        return acf;
    }

    public void setAcf(AlternateCareFacility acf) {
        this.acf = acf;
    }

    public Long getLiferayStateId() {
        return liferayStateId;
    }

    public void setLiferayStateId(Long liferayStateId) {
        this.liferayStateId = liferayStateId;
    }

    public Long getLiferayAcfId() {
        return liferayAcfId;
    }

    public void setLiferayAcfId(Long liferayAcfId) {
        this.liferayAcfId = liferayAcfId;
    }

    @Override
    public String getPassword() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Object getCredentials() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addPermission(GrantedPermission permission) {
        permissions.add(permission);
    }

    @Override
    public void removePermission(String permissionValue) {
        permissions.remove(permissionValue);
    }

    @Override
    public Collection<GrantedPermission> getAuthorities() {
        return permissions;
    }

    @Override
    public Object getDetails() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getPrincipal() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    public static boolean userHasAuthority(User auth, String authority) {
        return auth.getAuthorities().stream().anyMatch(ga -> ga.getAuthority().equals(authority));
    }

    public boolean isRoleAdmin() {
        return userHasAuthority(this, ROLE_ADMIN);
    }

}
