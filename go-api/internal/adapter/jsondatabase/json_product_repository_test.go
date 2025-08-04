package jsondatabase

import (
	"encoding/json"
	"os"
	"testing"

	"go-api/internal/domain/model"
)

func TestGetByID_ReturnsProduct(t *testing.T) {
	tmpFile, err := os.CreateTemp("", "products_*.json")
	if err != nil {
		t.Fatalf("failed to create temp file: %v", err)
	}
	defer os.Remove(tmpFile.Name())

	expected := model.Product{
		ID:          "123",
		Name:        "Producto de prueba",
		Description: "Description",
		Price:       100.0,
	}

	products := []model.Product{expected}

	if err := json.NewEncoder(tmpFile).Encode(products); err != nil {
		t.Fatalf("failed to write json: %v", err)
	}

	tmpFile.Close()

	repo := NewJSONProductRepository(tmpFile.Name())

	result, err := repo.GetByID("123")
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}

	if result.ID != expected.ID || result.Name != expected.Name {
		t.Errorf("expected %+v, got %+v", expected, result)
	}
}
