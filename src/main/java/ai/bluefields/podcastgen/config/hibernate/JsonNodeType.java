package ai.bluefields.podcastgen.config.hibernate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class JsonNodeType implements UserType<JsonNode> {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public int getSqlType() {
        return Types.OTHER;
    }

    @Override
    public Class<JsonNode> returnedClass() {
        return JsonNode.class;
    }

    @Override
    public boolean equals(JsonNode x, JsonNode y) {
        if (x == y) {
            return true;
        }
        if (x == null || y == null) {
            return false;
        }
        return x.equals(y);
    }

    @Override
    public int hashCode(JsonNode x) {
        return x.hashCode();
    }

    @Override
    public JsonNode nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) 
            throws SQLException {
        String json = rs.getString(position);
        if (json == null) {
            return null;
        }
        try {
            return mapper.readTree(json);
        } catch (Exception e) {
            throw new HibernateException("Could not deserialize JSON: " + json, e);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement st, JsonNode value, int index, SharedSessionContractImplementor session) 
            throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            try {
                st.setObject(index, value.toString(), Types.OTHER);
            } catch (Exception e) {
                throw new HibernateException("Could not serialize JsonNode to JSON string", e);
            }
        }
    }

    @Override
    public JsonNode deepCopy(JsonNode value) {
        if (value == null) {
            return null;
        }
        try {
            return mapper.readTree(value.toString());
        } catch (Exception e) {
            throw new HibernateException("Could not deep copy JsonNode", e);
        }
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(JsonNode value) {
        return value == null ? null : value.toString();
    }

    @Override
    public JsonNode assemble(Serializable cached, Object owner) {
        if (cached == null) {
            return null;
        }
        try {
            return mapper.readTree(cached.toString());
        } catch (Exception e) {
            throw new HibernateException("Could not assemble JsonNode", e);
        }
    }

    @Override
    public JsonNode replace(JsonNode detached, JsonNode managed, Object owner) {
        return deepCopy(detached);
    }
}
