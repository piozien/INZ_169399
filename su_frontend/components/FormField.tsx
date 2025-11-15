import type { ChangeEvent } from "react";

type FormFieldProps = {
  id: string;
  label: string;
  type: string;
  value: string;
  onChange: (e: ChangeEvent<HTMLInputElement>) => void;
  placeholder: string;
  disabled: boolean;
};

function FormField({
  id,
  label,
  type,
  value,
  onChange,
  placeholder,
  disabled,
}: FormFieldProps) {
  return (
    <div>
      <label
        htmlFor={id}
        className="block text-xs uppercase tracking-wider mb-3 text-[#BABABA]"
      >
        {label}
      </label>
      <input
        id={id}
        type={type}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        disabled={disabled}
        required
        className="w-full text-center py-4 px-3 rounded-[22px] bg-[#372C1E] text-[#BABABA] placeholder-neutral-400 focus:outline-none focus:ring-2 focus:ring-[#FF9D00]"
      />
    </div>
  );
}

export default FormField;
