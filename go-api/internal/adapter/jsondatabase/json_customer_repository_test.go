package jsondatabase

import (
	"encoding/json"
	"os"
	"testing"

	"go-api/internal/domain/model"

	"github.com/stretchr/testify/assert"
)

func TestGetByID(t *testing.T) {
	tempFile, err := os.CreateTemp("", "customers-*.json")
	assert.NoError(t, err)
	defer os.Remove(tempFile.Name())

	customer := model.Customer{
		ID:     "123",
		Name:   "John Doe",
		Email:  "test@mail.com",
		Status: model.Active,
	}

	data, _ := json.Marshal([]model.Customer{customer})
	_, err = tempFile.Write(data)
	assert.NoError(t, err)
	tempFile.Close()

	repo := NewJSONCustomerRepository(tempFile.Name())

	found, err := repo.GetByID("123")

	assert.NoError(t, err)
	assert.NotNil(t, found)
	assert.Equal(t, "John Doe", found.Name)
}
