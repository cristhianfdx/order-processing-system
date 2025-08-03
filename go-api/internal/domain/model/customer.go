package model

type CustomerStatus string

const (
	Active    CustomerStatus = "ACTIVE"
	Inactive  CustomerStatus = "INACTIVE"
	Suspended CustomerStatus = "SUSPENDED"
)

type Customer struct {
	ID     string         `json:"id"`
	Name   string         `json:"name"`
	Email  string         `json:"email"`
	Status CustomerStatus `json:"status"`
}
