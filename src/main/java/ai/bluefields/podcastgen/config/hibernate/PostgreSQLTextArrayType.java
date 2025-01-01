package ai.bluefields.podcastgen.config.hibernate;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.*;

public class PostgreSQLTextArrayType implements UserType<String[]> {

    @Override
    public int getSqlType() {
        return Types.ARRAY;
    }

    @Override
    public Class<String[]> returnedClass() {
        return String[].class;
    }

    @Override
    public boolean equals(String[] x, String[] y) {
        if (x == y) return true;
        if (x == null || y == null) return false;
        if (x.length != y.length) return false;
        for (int i = 0; i < x.length; i++) {
            if (!x[i].equals(y[i])) return false;
        }
        return true;
    }

    @Override
    public int hashCode(String[] x) {
        if (x == null) return 0;
        int result = 1;
        for (String s : x) {
            result = 31 * result + (s != null ? s.hashCode() : 0);
        }
        return result;
    }

    @Override
    public String[] deepCopy(String[] value) {
        if (value == null) return null;
        return value.clone();
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(String[] value) {
        return (Serializable) deepCopy(value);
    }

    @Override
    public String[] assemble(Serializable cached, Object owner) {
        return deepCopy((String[]) cached);
    }

    @Override
    public String[] replace(String[] detached, String[] managed, Object owner) {
        return deepCopy(detached);
    }

    @Override
    public String[] nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        Array array = rs.getArray(position);
        return array == null ? null : (String[]) array.getArray();
    }

    @Override
    public void nullSafeSet(PreparedStatement st, String[] value, int index, SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.ARRAY);
        } else {
            Array array = session.getJdbcConnectionAccess()
                    .obtainConnection()
                    .createArrayOf("text", value);
            st.setArray(index, array);
        }
    }
}
