package br.com.geraldao.bean;

import javax.persistence.Column;

public class ProcedureDefaultResult implements java.io.Serializable {

    private static final long serialVersionUID = 3812300860085791191L;
    private String            result;
    private String            resultDescription;
    private String            additionalDescription;

    @Column(name = "Result")
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Column(name = "ResultDescription")
    public String getResultDescription() {
        return resultDescription;
    }

    public void setResultDescription(String resultDescription) {
        this.resultDescription = resultDescription;
    }

    @Column(name = "AdditionalDescription")
    public String getAdditionalDescription() {
        return additionalDescription;
    }

    public void setAdditionalDescription(String additionalDescription) {
        this.additionalDescription = additionalDescription;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((additionalDescription == null) ? 0 : additionalDescription.hashCode());
        result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
        result = prime * result + ((resultDescription == null) ? 0 : resultDescription.hashCode());
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
        ProcedureDefaultResult other = (ProcedureDefaultResult) obj;
        if (additionalDescription == null) {
            if (other.additionalDescription != null)
                return false;
        } else if (!additionalDescription.equals(other.additionalDescription))
            return false;
        if (result == null) {
            if (other.result != null)
                return false;
        } else if (!result.equals(other.result))
            return false;
        if (resultDescription == null) {
            if (other.resultDescription != null)
                return false;
        } else if (!resultDescription.equals(other.resultDescription))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ProcedureDefaultResult [result=");
        builder.append(result);
        builder.append(", resultDescription=");
        builder.append(resultDescription);
        builder.append(", additionalDescription=");
        builder.append(additionalDescription);
        builder.append("]");
        return builder.toString();
    }

}
