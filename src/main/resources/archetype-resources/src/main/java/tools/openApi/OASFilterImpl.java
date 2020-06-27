package ${package}.tools.openApi;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * OpenApi filter implementation.
 */
public class OASFilterImpl implements OASFilter {

    /**
     * OAuth2 security token url
     */
    static final String APP_SECURITY_OAUTH2_TOKEN_URL =
            "app.security.oauth2.tokenUrl";

    /**
     * {@inheritDoc}
     */
    @Override
    public SecurityScheme filterSecurityScheme(SecurityScheme securityScheme) {

        Map<SecurityScheme.Type, Function<SecurityScheme, SecurityScheme>>
                securitySchemeMappers = new HashMap<>();
        securitySchemeMappers.put(
                SecurityScheme.Type.OAUTH2, this::modifyOauth2SecurityScheme);
//        securitySchemeMappers.put(
//                SecurityScheme.Type.HTTP, this::modifyHttpSecurityScheme);

        return Optional.of(securityScheme)
                .map(securitySchemeMappers.getOrDefault(
                        securityScheme.getType(), s -> s))
                .orElseThrow();
    }

    /**
     * Alters security scheme of Oauth2 type.
     *
     * @param securityScheme securityScheme to modify
     * @return modified securityScheme
     */
    SecurityScheme modifyOauth2SecurityScheme(
            SecurityScheme securityScheme) {
        Config config = getConfig();

        config.getOptionalValue(APP_SECURITY_OAUTH2_TOKEN_URL, String.class)
                .ifPresent(s -> {
            securityScheme.getFlows().getClientCredentials().setTokenUrl(s);
        });

        return securityScheme;
    }

    Config getConfig() {
        return ConfigProvider.getConfig();
    }

//    private SecurityScheme modifyHttpSecurityScheme(
//            SecurityScheme securityScheme) {
//        return securityScheme;
//    }


    @Override
    public void filterOpenAPI(OpenAPI openAPI) {

        /**
         * Workaround for issue:
         * https://github.com/OpenLiberty/open-liberty/issues/12723
         */
        var schemas = openAPI.getComponents().getSchemas().entrySet().stream()
                .filter(e -> StringUtils.isNotBlank(e.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue));

        openAPI.getComponents().setSchemas(schemas);
    }
}
